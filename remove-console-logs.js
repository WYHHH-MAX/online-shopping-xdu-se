const fs = require('fs');
const path = require('path');

// 定义要处理的文件夹路径
const directory = './frontend/src';

// 定义要处理的文件类型
const fileExtensions = ['.vue', '.js', '.ts'];

// 多个正则表达式匹配不同类型的console语句
const consolePatterns = [
  // 基本的console.log/error/warn/info/debug语句
  /console\.(log|error|warn|info|debug)\s*\(\s*([^)]*)\s*\)\s*;?/g,
  
  // 多行console语句，如包含模板字符串的情况
  /console\.(log|error|warn|info|debug)\s*\([\s\S]*?(?:\);|\),)/g,
  
  // 特殊情况：多行模板字符串
  /console\.(log|error|warn|info|debug)\s*\(`[\s\S]*?`\)\s*;?/g,
];

// 统计信息
let filesProcessed = 0;
let filesModified = 0;
let logsRemoved = 0;

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
          removeConsoleLogs(filePath);
        }
      } catch (err) {
        console.error(`无法访问 ${filePath}: ${err.message}`);
      }
    }
  } catch (err) {
    console.error(`无法读取目录 ${dir}: ${err.message}`);
  }
}

// 删除文件中的console日志
function removeConsoleLogs(filePath) {
  try {
    filesProcessed++;
    let content = fs.readFileSync(filePath, 'utf8');
    const originalContent = content;
    let matchCount = 0;
    
    // 尝试所有正则表达式模式
    for (const pattern of consolePatterns) {
      const matches = content.match(pattern) || [];
      matchCount += matches.length;
      
      // 替换当前模式的所有匹配项
      content = content.replace(pattern, '');
    }
    
    // 特别处理.vue文件中的脚本部分
    if (filePath.endsWith('.vue')) {
      // 清理<script>部分中的console语句
      content = cleanVueScriptSection(content, matchCount);
    }
    
    // 如果文件有变化，写回
    if (content !== originalContent) {
      fs.writeFileSync(filePath, content, 'utf8');
      filesModified++;
      console.log(`处理文件 [${filesProcessed}]: ${filePath} - 删除了 ${matchCount} 个控制台日志`);
      logsRemoved += matchCount;
    }
  } catch (err) {
    console.error(`处理文件 ${filePath} 时出错: ${err.message}`);
  }
}

// 专门处理Vue文件中的脚本部分
function cleanVueScriptSection(content, currentMatchCount) {
  let matchCount = currentMatchCount;
  
  // 找到<script>部分
  const scriptMatch = content.match(/<script[\s\S]*?>([\s\S]*?)<\/script>/);
  
  if (scriptMatch && scriptMatch[1]) {
    let scriptContent = scriptMatch[1];
    const originalScriptContent = scriptContent;
    
    // 对脚本内容应用所有正则表达式
    for (const pattern of consolePatterns) {
      const matches = scriptContent.match(pattern) || [];
      matchCount += matches.length;
      
      // 替换当前模式的所有匹配项
      scriptContent = scriptContent.replace(pattern, '');
    }
    
    // 如果脚本部分有变化，替换回原文件内容
    if (scriptContent !== originalScriptContent) {
      return content.replace(originalScriptContent, scriptContent);
    }
  }
  
  return content;
}

// 开始处理
console.log('开始删除前端项目中的控制台日志...');
const startTime = Date.now();

processDirectory(directory);

const endTime = Date.now();
const duration = (endTime - startTime) / 1000;

console.log('\n--- 完成 ---');
console.log(`处理文件总数: ${filesProcessed}`);
console.log(`修改文件数: ${filesModified}`);
console.log(`删除日志语句数: ${logsRemoved}`);
console.log(`耗时: ${duration.toFixed(2)}秒`); 