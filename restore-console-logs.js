const fs = require('fs');
const path = require('path');

// 定义要处理的文件夹路径
const directory = './frontend/src';

// 定义要处理的文件类型
const fileExtensions = ['.vue', '.js', '.ts'];

// 检查常见的需要日志的位置
const logPositions = [
  // 在try块后添加日志
  {
    pattern: /try\s*{([\s\S]*?)}\s*catch\s*\((.*?)\)\s*{([\s\S]*?)}/g,
    replacement: (match, tryBlock, errorVar, catchBlock) => {
      // 如果catch块为空或只有注释
      if (catchBlock.trim() === '' || catchBlock.trim().startsWith('//')) {
        return `try {${tryBlock}} catch (${errorVar}) {
  // console.error('操作失败:', ${errorVar});${catchBlock}
}`;
      }
      return match;
    }
  },
  // 在API调用后添加日志
  {
    pattern: /(await\s+[a-zA-Z0-9_$.]+\([^)]*\))(\s*;)/g,
    replacement: (match, apiCall, semi) => {
      // 提取API名称
      const apiName = apiCall.match(/await\s+([a-zA-Z0-9_$.]+)/);
      if (apiName && apiName[1]) {
        return `${apiCall}${semi} // console.log('${apiName[1]}调用结果:', 返回值);`;
      }
      return match;
    }
  },
  // 在函数开始处添加日志
  {
    pattern: /(async\s+)?function\s+([a-zA-Z0-9_]+)\s*\(([^)]*)\)\s*{/g,
    replacement: (match, async, funcName, params) => {
      return `${async || ''}function ${funcName}(${params}) {
  // console.log('${funcName}函数调用，参数:', ${params.split(',').map(p => p.trim().split(' ')[0]).filter(p => p).join(', ')});`;
    }
  },
  // 在fetch/axios调用后添加日志
  {
    pattern: /(const|let|var)\s+([a-zA-Z0-9_]+)\s*=\s*await\s+(fetch|axios)\(([^)]*)\)(\s*;)/g,
    replacement: (match, declType, varName, fetchType, url, semi) => {
      return `${declType} ${varName} = await ${fetchType}(${url})${semi}
  // console.log('${fetchType}请求结果:', ${varName});`;
    }
  }
];

// Vue文件中特殊处理
const vueComponentHooks = [
  // 在onMounted钩子中添加日志
  {
    pattern: /onMounted\s*\(\s*\(\)\s*=>\s*{/g,
    replacement: `onMounted(() => {
  // console.log('组件已挂载');`
  }
];

// 统计信息
let filesProcessed = 0;
let filesModified = 0;
let logsAdded = 0;

// 递归遍历目录处理文件
function processDirectory(dir) {
  try {
    const files = fs.readdirSync(dir);
    
    for (const file of files) {
      const filePath = path.join(dir, file);
      
      try {
        const stats = fs.statSync(filePath);
        
        if (stats.isDirectory()) {
          // 递归处理子目录
          processDirectory(filePath);
        } else if (stats.isFile() && fileExtensions.some(ext => file.endsWith(ext))) {
          // 处理匹配的文件
          restoreConsoleLogs(filePath);
        }
      } catch (err) {
        console.error(`无法访问 ${filePath}: ${err.message}`);
      }
    }
  } catch (err) {
    console.error(`无法读取目录 ${dir}: ${err.message}`);
  }
}

// 恢复被删除的console语句
function restoreConsoleLogs(filePath) {
  try {
    filesProcessed++;
    let content = fs.readFileSync(filePath, 'utf8');
    const originalContent = content;
    let restoredCount = 0;
    
    // 应用常规的日志恢复模式
    for (const position of logPositions) {
      const originalLength = content.length;
      content = content.replace(position.pattern, position.replacement);
      
      // 检查是否有变化
      if (content.length !== originalLength) {
        restoredCount++;
      }
    }
    
    // 特别处理.vue文件
    if (filePath.endsWith('.vue')) {
      for (const hook of vueComponentHooks) {
        const originalLength = content.length;
        content = content.replace(hook.pattern, hook.replacement);
        
        // 检查是否有变化
        if (content.length !== originalLength) {
          restoredCount++;
        }
      }
    }
    
    // 如果文件有变化，写回
    if (content !== originalContent) {
      fs.writeFileSync(filePath, content, 'utf8');
      filesModified++;
      logsAdded += restoredCount;
      console.log(`恢复文件 [${filesProcessed}]: ${filePath} - 添加了 ${restoredCount} 个日志注释`);
    }
  } catch (err) {
    console.error(`处理文件 ${filePath} 时出错: ${err.message}`);
  }
}

// 开始处理
console.log('开始恢复被删除的console语句（注释形式）...');
const startTime = Date.now();

processDirectory(directory);

const endTime = Date.now();
const duration = (endTime - startTime) / 1000;

console.log('\n--- 完成 ---');
console.log(`处理文件总数: ${filesProcessed}`);
console.log(`修改文件数: ${filesModified}`);
console.log(`添加日志注释数: ${logsAdded}`);
console.log(`耗时: ${duration.toFixed(2)}秒`); 