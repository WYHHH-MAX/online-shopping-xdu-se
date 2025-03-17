# GitHub推送指南

本文档提供将项目推送到GitHub仓库的步骤指南。

## 前提条件

1. 已安装Git
2. 已创建GitHub账号
3. 已创建GitHub仓库

## 步骤

### 1. 检查当前Git配置

如果你的仓库已经初始化过Git，先检查当前的远程仓库设置：

```bash
git remote -v
```

### 2. 设置远程仓库

如果尚未设置远程仓库，或需要修改远程仓库，使用以下命令：

```bash
# 添加远程仓库
git remote add origin https://github.com/你的用户名/仓库名.git

# 如果需要修改现有的远程仓库URL
git remote set-url origin https://github.com/你的用户名/仓库名.git
```

### 3. 拉取最新代码（可选）

如果GitHub仓库已有内容，先拉取最新代码：

```bash
git pull origin main
```

注意：如果你使用的是master分支而不是main，请相应替换命令中的分支名。

### 4. 提交本地更改

```bash
# 添加所有更改的文件
git add .

# 提交更改
git commit -m "描述你的更改内容"
```

### 5. 推送到GitHub

```bash
# 推送到远程仓库的main分支
git push -u origin main
```

如果遇到冲突，需要先拉取最新代码并解决冲突：

```bash
git pull origin main
# 解决冲突后
git push -u origin main
```

### 6. 常见问题排查

#### 认证失败

如果遇到认证失败的问题，可能需要：
- 使用Personal Access Token登录（GitHub不再支持直接密码认证）
- 配置SSH密钥

##### 使用Personal Access Token：

1. 在GitHub的Settings > Developer settings > Personal access tokens生成token
2. 使用token作为密码：
```bash
git remote set-url origin https://你的用户名:你的token@github.com/你的用户名/仓库名.git
```

##### 配置SSH密钥：

1. 生成SSH密钥：
```bash
ssh-keygen -t rsa -b 4096 -C "你的邮箱@example.com"
```

2. 将公钥添加到GitHub：Settings > SSH and GPG keys

3. 使用SSH URL设置远程仓库：
```bash
git remote set-url origin git@github.com:你的用户名/仓库名.git
```

### 7. .gitignore设置

确保你的仓库有适当的.gitignore文件，避免推送不必要的文件，如编译产物、IDE配置等。

对于Java项目，建议包含：
- target/
- *.class
- .idea/
- .vscode/
- *.iml
- .DS_Store

### 8. 提交成功验证

推送完成后，访问你的GitHub仓库页面，确认更改已成功提交。

## 后续更新

每次有新的更改时，只需要重复步骤4和5：

```bash
git add .
git commit -m "新的更改描述"
git push origin main
``` 