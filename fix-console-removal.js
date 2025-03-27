const fs = require('fs');
const path = require('path');

// 定义要处理的文件夹路径
const directory = './frontend/src';

// 定义要处理的文件类型
const fileExtensions = ['.vue', '.js', '.ts'];

// 检查并修复的正则表达式模式
const errorPatterns = [
  // 处理被错误删除的逗号后内容
  { 
    pattern: /,(\s*[}\])])/g, 
    replacement: '$1' 
  },
  // 处理连续的分号
  { 
    pattern: /;{2,}/g, 
    replacement: ';' 
  },
  // 处理缺失闭合括号的模板字符串
  { 
    pattern: /(`.*?)(\);?|,\s*\n)\s*}/g,
    replacement: (match, p1, p2) => {
      if (!p1.includes('}')) {
        return p1 + '`' + p2 + ' }';
      }
      return match;
    }
  },
  // 处理缺失分号
  {
    pattern: /}\s*(const|let|var|function|class|if|for|while)/g,
    replacement: '};\n$1'
  },
  // 处理空表达式
  {
    pattern: /\(\s*\)(\s*;)/g,
    replacement: '(/* 修复空参数 */)$1'
  },
  // 处理空catch块
  {
    pattern: /catch\s*\(([^)]+)\)\s*{(\s*)}/g,
    replacement: 'catch ($1) {\n    // 错误处理被移除\n$2}'
  }
];

// Vue文件中特殊处理的模式
const vuePatterns = [
  // 处理script部分的缺失闭合
  {
    pattern: /<script[\s\S]*?>[^<]*?(<\/script>)/g,
    checkFunction: (content) => {
      const scriptOpenCount = (content.match(/<script/g) || []).length;
      const scriptCloseCount = (content.match(/<\/script>/g) || []).length;
      return scriptOpenCount !== scriptCloseCount;
    },
    fixFunction: (content) => {
      const scriptStartIndex = content.indexOf('<script');
      if (scriptStartIndex !== -1) {
        const scriptEndTest = content.match(/<\/script>/g);
        if (!scriptEndTest) {
          return content + '\n</script>';
        }
      }
      return content;
    }
  }
];

// 统计信息
let filesProcessed = 0;
let filesFixed = 0;
let errorsFixed = 0;

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
          fixConsoleRemovalIssues(filePath);
        }
      } catch (err) {
        console.error(`无法访问 ${filePath}: ${err.message}`);
      }
    }
  } catch (err) {
    console.error(`无法读取目录 ${dir}: ${err.message}`);
  }
}

// 修复删除console语句导致的问题
function fixConsoleRemovalIssues(filePath) {
  try {
    filesProcessed++;
    let content = fs.readFileSync(filePath, 'utf8');
    const originalContent = content;
    let fixCount = 0;
    
    // 对常规代码应用修复模式
    for (const pattern of errorPatterns) {
      const matches = content.match(pattern.pattern) || [];
      fixCount += matches.length;
      
      // 应用修复
      content = content.replace(pattern.pattern, pattern.replacement);
    }
    
    // 特别处理.vue文件
    if (filePath.endsWith('.vue')) {
      for (const pattern of vuePatterns) {
        if (pattern.checkFunction(content)) {
          const newContent = pattern.fixFunction(content);
          if (newContent !== content) {
            content = newContent;
            fixCount++;
          }
        }
      }
    }
    
    // 检查是否需要恢复try-catch块中的错误处理
    if (content.includes('try {') && !content.includes('catch')) {
      content = content.replace(/try\s*{([\s\S]*?)}/g, (match, p1) => {
        return `try {${p1}} catch (error) {\n  // 错误处理被恢复\n}`;
      });
      fixCount++;
    }
    
    // 如果文件有变化，写回
    if (content !== originalContent) {
      fs.writeFileSync(filePath, content, 'utf8');
      filesFixed++;
      errorsFixed += fixCount;
      console.log(`修复文件 [${filesProcessed}]: ${filePath} - 修复了 ${fixCount} 个问题`);
    }
  } catch (err) {
    console.error(`处理文件 ${filePath} 时出错: ${err.message}`);
  }
}

// 开始处理
console.log('开始修复因删除控制台日志导致的问题...');
const startTime = Date.now();

processDirectory(directory);

const endTime = Date.now();
const duration = (endTime - startTime) / 1000;

console.log('\n--- 完成 ---');
console.log(`处理文件总数: ${filesProcessed}`);
console.log(`修复文件数: ${filesFixed}`);
console.log(`修复问题数: ${errorsFixed}`);
console.log(`耗时: ${duration.toFixed(2)}秒`); 