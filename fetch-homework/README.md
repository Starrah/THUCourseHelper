### 获取网络学堂课程数据模块代码编译说明
`main.ts`是主代码文件，包括了执行该任务所需的全部代码：建立Android和Web之间的接口、调用thu-learn-lib库，等等。  

### 编译以在安卓程序中使用
1. 切换到`fetch-homework目录。`
2. 如果修改了`main.ts`，需要使用Typescript编译生成`main.js`。  
3. 如果未安装依赖，需要`npm install`。  
4. 运行`webpack`。这样会自动在安卓的`app/src/main/assets/www`目录下产生`homework-bundled.js`。  
5. 需要手动把`homework-index.html`复制到`app/src/main/assets/www`目录下去。  