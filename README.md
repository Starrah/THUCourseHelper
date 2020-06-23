清华课程小助手
==================
一款真正懂清华学生的android课程管理APP

**github地址**

前端：https://github.com/Starrah/THUCourseHelper

后端：https://github.com/Starrah/THUCourseHelperBackend

## 基本信息

### 1.开发环境

- Kotlin 1.3.71
- Android最低API级别 26
- AndroidAPI级别 29
- Gradle 5.6.4

### 2.目录结构

/app/src/main/java：代码目录

- ​	activity：日程详情显示和编辑
- ​	data：数据定义和数据库
- ​	fragment：MainActivity的几个Fragment，还有登录界面
- ​	information：信息显示模块
- ​	onlinedata：前后端交互和数据源定义
- ​	picker：周选择器
- ​	remind：提醒模块
- ​	service://todo
- ​	table：课程/日程表
- ​	utils：其他实用的函数
- ​	widget：小部件和常驻通知栏

/app/src/main/res：资源目录

- ​	layout/calendar_item_edit：详情编辑
- ​	layout/calendar_item_edit：详情显示
- ​	layout/fragment：MainActivity的几个Fragment，还有登录界面
- ​	layout/information：信息显示
- ​	layout/picker：周选择器
- ​	layout/table：课程/日程表
- ​	layout/widget：小部件和常驻通知栏
- ​	menu：下方导航栏
- ​	xml：小部件定义
- ​	drawable：素材
- ​	values：常量和颜色等定义

## 功能介绍和亮点说明

### 1.加载与主界面

在进入主界面之前，我们会显示加载界面几秒钟，此时后台进行数据的加载。

主界面使用BottomNavigationView作为底部导航栏，拥有四个可以互相切换的Fragment

![main](https://github.com/Starrah/THUCourseHelper/raw/master/preview/main.gif)

### 2.课程/日程表

#### 2.1 能横向，纵向滑动的课程表

我们使用两层scrollview来实现这一功能：一方面，上方的日期是一个横向滑动的scrollview；另一方面，下方是一个纵向scrollview套一个横向scrollview。之后，为了让上方的日期和下方的课程同时滑动，我们使用了绑定的方法：当监听到其中一个scrollview滑动的时候，自动将另一个scrollview滑动到对应位置[[1]](https://blog.csdn.net/lixpjita39/article/details/73180546)。

![table](https://github.com/Starrah/THUCourseHelper/raw/master/preview/table.gif)

#### 2.2 根据设置动态设置ui，显示日程

我们在每次onstart时，都会重新设置ui---根据设定的常数和读取的手机屏幕大小，全局设置等更新各个控件大小，来实现切换显示方式等功能，以及适配不同手机。

我们在每次onstart时也会刷新日程显示，具体方法是清空课程显示位置的所有日程时间段view，然后根据数据库读取结果动态放置日程时间段。其中，日程时间段的放置位置由课程数据计算得到，其颜色由其日程做哈希得到，而且每个时间段的Tag都会被设置成对应日程id，便于点击进入详情显示界面。

#### 2.3 边框的显示

日期，时间这些地方的实线边框，我们通过设置背景图为有实线边框的图片实现。

纵向的虚线边框，我们通过设置每个日程放置位置（也就是周一-周日下方的一整个长条）的背景图来实现，这里的背景图是透明，只有虚线右边框的，以显示背景图片，以及保证虚线边框不堆在一起。

横向的虚线边框，我们通过代码动态设置，也就是把一条横虚线放在每个放置位置的对应高度。

同时，为了保证虚线边框和背景图片相适应（比如空白背景需要黑色边框，绿色背景需要白色边框），我们给每张背景图片设置了边框颜色，在onStart设置边框时根据背景图片修改边框颜色。

### 3.详情显示

我们在上方显示整个日程的整体信息，然后在下方通过scrollview来显示每个时间段信息。时间段信息是动态加载到scrollview上的。

![show](https://github.com/Starrah/THUCourseHelper/raw/master/preview/show.jpg)

### 4.详情编辑

#### 4.1 主体结构

由于每个日程有多个时间段，而且时间段可以动态增，删，改，我们采用recyclerview来放置每个时间段，用onBindViewHolder函数来进行动态更新。

那么我们是如何实现根据各种选项动态更新界面的呢？首先，我们在xml里定义了所有可能出现的编辑行，在onBindViewHolder函数里动态根据这个时间段的信息来显示，隐藏编辑行。其次，我们给每个选择器和文本框都绑定了监听事件，一旦监听到这些位置的值发生改变，就立即更新recyclerview的数据，之后recyclerview会根据更新后的数据来更新显示。

![edit](https://github.com/Starrah/THUCourseHelper/raw/master/preview/edit.jpg)

#### 4.2 多级选择器

多级选择器在我们的项目中应用广泛。设置日期和时间，设置大节-小节情况，设置各种选项都需要这种选择器。我们采用了一个开源库PickerView[[2]](https://github.com/Bigkoo/Android-PickerView)，它被广泛应用于招行信用卡的“掌上生活”等商业软件中。

![multiple_picker](https://github.com/Starrah/THUCourseHelper/raw/master/preview/multiple_picker.jpg)

#### 4.3 周选择器

我们在开源项目SimpleDayPicker[[3]](https://github.com/informramiz/SimpleDayPicker)上做了大量修改，完成了周选择器。原项目只支持周一-周日的多选，我们修改之后，支持了根据学期信息修改周选择，以及选择单周，双周，前半学期，全学期等快捷键。

![week_picker](https://github.com/Starrah/THUCourseHelper/raw/master/preview/week_picker.jpg)

### 5.信息显示

//todo 后端

在前端部分，我们接受后端传来的json，解析得到对应的服务名称与URL，并且动态创建按钮，每个按钮绑定前往对应URL的intent，实现了信息显示。值得一提的是，对于空教室显示，我们通过监听搜索框与字符串匹配实现了教室搜索功能，这样大大改进了用户体验，因为原来教室有几十个，很难找到自己要的教室。

![information](https://github.com/Starrah/THUCourseHelper/raw/master/preview/information.gif)

### 6.设置界面与登录

#### 6.1 设置界面

我们的设置界面直接使用安卓原生的PreferenceFragmentCompat，扩展和修改非常方便。当然，因为原生界面的限制，我们只能强制在设置界面使用白背景，否则文字直接放在背景上比较难看。

#### 6.2 登录

首先，因为学校选课系统的登录需要验证码，我们的登录界面对于验证码的处理就很重要。我们用一个ImageView来放置验证码，已进入登录界面就发送请求获取验证码图片。我们还给这个ImageView绑定点击事件来点击切换验证码。因为学校选课系统的验证码固定只有数字和大写字母，我们还通过结果转大写实现了验证码不区分大小写功能。

其次，我们设置了一些小功能来让登录更加用户友好，比如长按按钮显示密码，登录后会显示一个加载控件等。

### 7.小部件与常驻通知栏

我们的小部件和常驻通知栏要实现动态更新数据，以及通过上下按钮来切换显示。我们的实现思路如下：首先，我们使用BroadcastReceiver来接收更新小部件/常驻通知栏显示的广播，并且给上/下按钮绑定切换显示日程的广播。其次，为了数据相对持久的存储，我们用静态变量来存储对应的日程数据。最终，//todo：动态更新

![widget](https://github.com/Starrah/THUCourseHelper/raw/master/preview/widget.gif)

### 8.提醒模块

### 9.数据定义

### 10.清华数据获取

### 11.后端服务



### 12.功能亮点

- 能横向，纵向滑动的课程/日程表
- 课程/日程表边框的显示
- 详情编辑的界面动态更新
- 周选择器
- 用户友好的登录界面
- 小部件和常驻通知栏
