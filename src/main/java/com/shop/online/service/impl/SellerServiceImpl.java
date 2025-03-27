package com.shop.online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.online.common.result.PageResult;
import com.shop.online.dto.SellerApplyDTO;
import com.shop.online.entity.Seller;
import com.shop.online.entity.User;
import com.shop.online.exception.BusinessException;
import com.shop.online.mapper.SellerMapper;
import com.shop.online.service.SellerService;
import com.shop.online.service.UserService;
import com.shop.online.vo.SellerVO;
import com.shop.online.vo.auth.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 商家服务实现类
 */
@Service
@Slf4j
public class SellerServiceImpl extends ServiceImpl<SellerMapper, Seller> implements SellerService {

    @Autowired
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean apply(SellerApplyDTO sellerApplyDTO) {
        try {
            log.info("开始处理商家入驻申请: {}", sellerApplyDTO);
            
            User currentUser = null;
            
            // 判断是否提供了用户名和密码（未登录用户）
            if (StringUtils.hasText(sellerApplyDTO.getUsername()) && StringUtils.hasText(sellerApplyDTO.getPassword())) {
                // 未登录用户提交申请，需要创建新用户
                log.info("检测到未登录用户申请，创建新用户: {}", sellerApplyDTO.getUsername());
                
                try {
                    // 检查用户名是否已存在
                    userService.checkUsernameUnique(sellerApplyDTO.getUsername());
                    
                    // 创建新用户
                    RegisterRequest registerRequest = new RegisterRequest();
                    registerRequest.setUsername(sellerApplyDTO.getUsername());
                    registerRequest.setPassword(sellerApplyDTO.getPassword());
                    registerRequest.setNickname(StringUtils.hasText(sellerApplyDTO.getNickname()) ? 
                                               sellerApplyDTO.getNickname() : sellerApplyDTO.getUsername());
                    
                    userService.register(registerRequest);
                    log.info("新用户创建成功: {}", sellerApplyDTO.getUsername());
                    
                    // 获取新创建的用户
                    LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
                    userQuery.eq(User::getUsername, sellerApplyDTO.getUsername());
                    currentUser = userService.getCurrentUser();
                    
                    if (currentUser == null) {
                        throw new BusinessException("用户创建成功但无法获取用户信息，请尝试登录后再申请");
                    }
                } catch (Exception e) {
                    log.error("创建新用户失败: {}", e.getMessage(), e);
                    throw new BusinessException("创建用户失败: " + e.getMessage());
                }
            } else {
                // 已登录用户提交申请
                currentUser = userService.getCurrentUser();
                if (currentUser == null) {
                    log.error("当前未登录且未提供注册信息");
                    throw new BusinessException("请先登录或提供完整的注册信息");
                }
            }
            
            log.info("当前用户信息: id={}, username={}, role={}", 
                     currentUser.getId(), currentUser.getUsername(), currentUser.getRole());

            // 检查用户角色是否为买家
            if (currentUser.getRole() != 0) {
                log.error("用户角色不是买家，无法申请成为卖家, userId={}, role={}", 
                         currentUser.getId(), currentUser.getRole());
                throw new BusinessException("已经是卖家或者管理员，无法再次申请");
            }

            // 检查是否已经提交过申请
            LambdaQueryWrapper<Seller> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Seller::getUserId, currentUser.getId());
            Seller existingSeller = getOne(queryWrapper);
            
            log.info("检查用户是否已有申请记录: userId={}, hasRecord={}", 
                     currentUser.getId(), existingSeller != null);
            
            if (existingSeller != null) {
                if (existingSeller.getStatus() == 0) {
                    log.error("用户已提交过申请，正在审核中, userId={}", currentUser.getId());
                    throw new BusinessException("您已提交申请，正在审核中");
                } else if (existingSeller.getStatus() == 1) {
                    log.error("用户已经是卖家, userId={}", currentUser.getId());
                    throw new BusinessException("您已经是卖家");
                } else if (existingSeller.getStatus() == 2) {
                    log.info("用户之前的申请被拒绝，可以重新申请, userId={}", currentUser.getId());
                    // 重新申请，使用已有记录
                    existingSeller.setShopName(sellerApplyDTO.getShopName());
                    existingSeller.setShopDesc(sellerApplyDTO.getDescription());
                    existingSeller.setContactName(sellerApplyDTO.getContactName());
                    existingSeller.setContactPhone(sellerApplyDTO.getContactPhone());
                    existingSeller.setContactEmail(sellerApplyDTO.getContactEmail());
                    existingSeller.setBusinessLicense(sellerApplyDTO.getBusinessLicense());
                    existingSeller.setStatus(0); // 重置为待审核
                    existingSeller.setRejectReason(null); // 清空拒绝原因
                    existingSeller.setUpdatedTime(LocalDateTime.now());
                    boolean updateResult = updateById(existingSeller);
                    log.info("更新已有申请记录结果: {}", updateResult);
                    return updateResult;
                }
            }

            // 创建新的商家记录
            Seller seller = new Seller();
            // 手动设置字段，避免名称不匹配的问题
            seller.setUserId(currentUser.getId()); // 使用用户ID关联用户
            seller.setShopName(sellerApplyDTO.getShopName());
            seller.setShopDesc(sellerApplyDTO.getDescription()); // 注意字段名转换
            seller.setContactName(sellerApplyDTO.getContactName());
            seller.setContactPhone(sellerApplyDTO.getContactPhone());
            seller.setContactEmail(sellerApplyDTO.getContactEmail());
            seller.setBusinessLicense(sellerApplyDTO.getBusinessLicense());
            seller.setStatus(0); // 待审核
            seller.setCreatedTime(LocalDateTime.now());
            seller.setUpdatedTime(LocalDateTime.now());
            seller.setDeleted(0);

            log.info("提交商家入驻申请, userId={}, shopName={}", 
                     currentUser.getId(), seller.getShopName());
            boolean saveResult = save(seller);
            log.info("保存新申请记录结果: {}", saveResult);
            return saveResult;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("处理商家入驻申请出错: {}", e.getMessage(), e);
            throw new BusinessException("申请失败: " + e.getMessage());
        }
    }

    @Override
    public Seller getSellerInfo(Long sellerId) {
        Seller seller = getById(sellerId);
        if (seller == null || seller.getDeleted() == 1) {
            log.error("商家不存在, sellerId={}", sellerId);
            throw new BusinessException("商家不存在");
        }
        return seller;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(Long sellerId, boolean approved, String rejectReason) {
        Seller seller = getById(sellerId);
        if (seller == null || seller.getDeleted() == 1) {
            log.error("商家不存在, sellerId={}", sellerId);
            throw new BusinessException("商家不存在");
        }

        if (seller.getStatus() != 0) {
            log.error("商家状态不是待审核, sellerId={}, status={}", sellerId, seller.getStatus());
            throw new BusinessException("商家已审核");
        }

        // 更新商家状态
        seller.setStatus(approved ? 1 : 2); // 1-审核通过，2-审核拒绝
        if (!approved) {
            seller.setRejectReason(rejectReason);
        }
        seller.setUpdatedTime(LocalDateTime.now());
        boolean updated = updateById(seller);

        // 如果审核通过，更新用户角色为卖家
        if (approved && updated) {
            User user = userService.getUserById(seller.getUserId());
            if (user != null) {
                user.setRole(1); // 卖家角色
                updated = userService.updateUserRole(user.getId(), 1);
                log.info("更新用户角色为卖家, userId={}", user.getId());
            }
        }

        return updated;
    }

    @Override
    public Seller getCurrentSeller() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            log.error("当前未登录");
            throw new BusinessException("请先登录");
        }

        // 检查用户角色是否为卖家
        if (currentUser.getRole() != 1) {
            log.error("用户角色不是卖家, userId={}, role={}", currentUser.getId(), currentUser.getRole());
            throw new BusinessException("您不是卖家");
        }
        
        // 通过用户ID查询商家信息
        LambdaQueryWrapper<Seller> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Seller::getUserId, currentUser.getId())
                   .eq(Seller::getStatus, 1) // 确保状态是已审核通过
                   .eq(Seller::getDeleted, 0);
                   
        return getOne(queryWrapper);
    }

    @Override
    public PageResult<Seller> getPendingSellers(int page, int size) {
        LambdaQueryWrapper<Seller> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Seller::getStatus, 0) // 待审核
                .eq(Seller::getDeleted, 0) // 未删除
                .orderByAsc(Seller::getCreatedTime); // 按创建时间升序排列
        
        Page<Seller> pageParam = new Page<>(page, size);
        Page<Seller> pageResult = page(pageParam, queryWrapper);
        
        return new PageResult<>(pageResult.getTotal(), pageResult.getRecords());
    }

    @Override
    public boolean uploadQualification(Long sellerId, String fileType, String filePath) {
        Seller seller = getById(sellerId);
        if (seller == null || seller.getDeleted() == 1) {
            log.error("商家不存在, sellerId={}", sellerId);
            throw new BusinessException("商家不存在");
        }

        // 根据文件类型更新对应的字段
        switch (fileType) {
            case "logo":
                seller.setShopLogo(filePath);
                break;
            case "businessLicenseImage":
                seller.setBusinessLicenseImage(filePath);
                break;
            case "idCardFront":
                seller.setIdCardFront(filePath);
                break;
            case "idCardBack":
                seller.setIdCardBack(filePath);
                break;
            default:
                log.error("不支持的文件类型, fileType={}", fileType);
                throw new BusinessException("不支持的文件类型");
        }

        seller.setUpdatedTime(LocalDateTime.now());
        return updateById(seller);
    }

    @Override
    public SellerVO getSellerByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        
        LambdaQueryWrapper<Seller> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Seller::getUserId, userId)
                    .eq(Seller::getStatus, 1)  // 只查询已审核通过的商家
                    .eq(Seller::getDeleted, 0);
        
        Seller seller = getOne(queryWrapper);
        if (seller == null) {
            return null;
        }
        
        SellerVO sellerVO = new SellerVO();
        BeanUtils.copyProperties(seller, sellerVO);
        return sellerVO;
    }

    @Override
    public SellerVO createSeller(Seller seller) {
        boolean saved = save(seller);
        if (!saved) {
            throw new BusinessException("创建商家失败");
        }
        
        SellerVO sellerVO = new SellerVO();
        BeanUtils.copyProperties(seller, sellerVO);
        return sellerVO;
    }

    @Override
    public SellerVO updateSeller(Seller seller) {
        Seller existingSeller = this.getById(seller.getId());
        if (existingSeller == null) {
            log.error("商家不存在, sellerId={}", seller.getId());
            throw new BusinessException("商家不存在");
        }
        
        // 保留一些不能由客户端更新的字段
        seller.setCreatedTime(existingSeller.getCreatedTime());
        seller.setDeleted(existingSeller.getDeleted());
        seller.setUserId(existingSeller.getUserId());  // 用户ID不允许修改
        
        // 更新时间将由MyBatis-Plus自动设置
        
        // 更新商家信息
        log.info("更新商家信息: id={}, shopName={}", seller.getId(), seller.getShopName());
        this.updateById(seller);
        
        // 转换为VO返回
        SellerVO sellerVO = new SellerVO();
        BeanUtils.copyProperties(seller, sellerVO);
        return sellerVO;
    }
} 