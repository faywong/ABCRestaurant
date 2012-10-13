1 the structure of the archive is:
.
├── ABCRestaurant_CLIENT_APP -----------------------Application that intented to be runned on Android Tablet(Such as Lenovo S7 Tablet
│   ├── AndroidManifest.xml
│   ├── assets
│   ├── bin
│   ├── gen
│   ├── proguard-project.txt
│   ├── project.properties
│   ├── res
│   └── src
├── ABCRestaurant_SERVER_SCRIPT---------------------Script that response to the client request(such as saving the order of user, providing management interface to manager)
│   ├── application
│   ├── index.php
│   ├── license.txt
│   ├── system
│   └── user_guide
└── TEST_DB-----------------------------------------Datebase data that used to test the programme
    └── ABCRestaurant.sql

11 directories, 6 files

2 Other comments:

Hello everyone, I am wangfei<wangfei584521@163.com>.

I write this small application which is expected to be some useful for restaurateurs in my spare time of last week(when after work).

As I single man do the design, Coding(Android client app & server PHP script which used to receive order information from client app 
and operate on Mysql database), debug, UI(though rather ugly), so this is just an demo, it's not powerful, and need two much refactoring to be more high-quality.

Let's take a look on it.
:):)


Expecting you go a restaurant for lunch, you sit there and restaurateur leave you a tablet on the table,
you can use it to select the course, and order it, after your simple operation, you can final order, and
once you confirm, the order info will be sent to the server in the restaurant, with the high speed of information flow, 
in the view of restaurateur, they can track the every order/course more precisely, saving more service-running cost,
 customers can help themselves to take order, no need to embrassed about some other waiter waiting in the near.

The order is made of one course->Chuancai1
so let's send it to the server now!
so it has been added to the "abc_orders" table of Mysql database "ABCRestaurant"
let's check it on the mamagement panel.

I take a second try now!
This time I have orderred two courses. 
Let's check it twice.
In ther latest Order(Order id is 21), there are two courses("Chuancai1" and "Ecai1").

The development tools I used are:
1 LAMP(Linux + apache + mysql + php) to construct the Server service
	I used the smart MVC php development framework----CodeIgniter.
2 Zend Studio which is used to edit the PHP script
3 Eclipse & JDK & Android ADT & Android SDK

and that's all, thanks for you interest!
