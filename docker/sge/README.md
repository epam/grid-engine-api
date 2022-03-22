# Configuring IDEA to run SGE grid-engine in Docker container and be able to attach the debug process

1. Docker IDEA plugin should be pre-installed. 
2. Should be pre-installed docker-compose version not lower 1.29.2, or docker desktop version not lower 3.5.2
3. Follow to Gradle tab (right upper part of IDE window) and chose Tasks -> docker -> composeUp
4. Run composeUp task, under the hood it will clean project, re-build jar file, build docker image and finally run a container with application.
5. After execution of composeUp task in the "Services" tab
    (row of tabs in the bottom line of IDEA window) in left down part of window you will find item "Docker". 
    Choose it and find inside running sge-container. Click it, and you will see the sub-tab "Log" containing the log-output of running application.
    You need to find the line that contains "Listening for transport dt_socket at address: 5005".
    To the right of this line you will see the link "Attach debugger" (this link seems quite pale).
    Follow this link and debug session will start.
    Use IDEA Debugger to debug a containerized application.
6. When you`re done with all manipulation, follow to Gradle tab and find Tasks -> docker -> composeDown. Run this task to shut down SGE container.    
