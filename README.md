Installation steps:
Linux :
	#Step1: install headless java application
		sudo apt-get install -y unzip openjdk-8-jre-headless xvfb libxi6 libgconf-2-4
	#Step2: set java home path and permissions of application
		set JAVA_HOME = "/usr"
		set permissions as 777 for "driver" folder inside the project
	#Step3: installing chrome browser
		sudo apt-get install google-chrome-stable
	#Step4: install and enable xvfb to create virtual display used by chromedriver
		sudo apt-get update
		sudo apt-get install xvfb
		xvfb :99 -ac -screen 0 1920x1080x24 & export DISPLAY=:99
	#Step5: install postgres database for application
		append postgres configuration files as follows 
		a) pg_hba.conf
		append file contents with 
			host    all             all              0.0.0.0/0                      trust
			host    all             all              ::/0                           trust
			host    all    			all    			all    							trust
		b) postgresql.conf
		modify this file as follows
			listen_addresses = '*'
			(instead of listen_addresses = 'localhost')
		c) reset postgres password
			sudo passwd postgres
		

other helpful commands for Ubuntu machine:
======================================================
a) for changing postgres user password
sudo su - postgres
psql
ALTER USER postgres WITH PASSWORD 'postgres';
/q
exit

b) postgres installation commands in following URL
	https://www.godaddy.com/garage/how-to-install-postgresql-on-ubuntu-14-04/
	Eg:
	sudo apt-get install postgresql postgresql-contrib
	
	
c) uninstalling postgres :
#uninstall
sudo apt-get purge 'postgresql-*'
sudo apt-get autoremove 'postgresql-*'
sudo service postgresql status
d) uninstalling java :
sudo apt-get purge 'java*'
sudo apt-get purge 'jdk*'
sudo apt-get purge 'jre*'

e)update OS packages any point of time
	sudo apt-get update

======================================================


Windows :
	#Step1:installing chrome browser
 
Deployment process : 

Please find some notes below for deploying the application:

1) /driver – folder contains chrome drivers for both windows and linux machines
2) /properties – folder contains environment specific property files for the application
3) Build jars:
	a) While in the root folder, execute “mvn package” command to generate the executable jars
	b) The jars will be generated in target folder of MaerskCarrier, AclCarrier, MscCarrier folders
4) Deployment:
	a) Copy the generated jars to their respective container
	b) Modify the following properties in application-uat.properties file under properties folder
		spring.datasource.url - jdbc:postgresql://<db_host>:<db_port>/<db_name>?stringtype=unspecified
		spring.datasource.username – db connection username
		spring.datasource.password – db connection password
		driver.path - <full path to driver folder>/chromedriver (for linux)/ <full path to driver folder>/chromedriver.exe (windows)
		cron.expression – cron expression for scheduling the scraping job
		logging.file - <full path to store the log file>/<log file name>.log
		Database schema will be created after the application has started running
	c) Copy driver and properties folders to all the containers

	d) Execute this command to run the application - java -jar <carrier_jar>.jar --spring.config.additional-location=<folder path to properties folder – should end with /> --spring.profiles.active=uat --server.port=<port on which the app should run on(optional if app is running on different containers, default port is 8080)>
#Eg:
#java -jar AclCarrier/target/AclCarrier.jar --spring.config.additional-location=./properties/ --spring.profiles.active=local --server.port=8081


#TODO: Prepare full setup script for complete application to run