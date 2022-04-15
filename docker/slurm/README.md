# Configuring IDEA to run SLURM grid-engine in Docker container and be able to attach the debug process

1. Docker IDEA plugin should be pre-installed.
2. Should be pre-installed docker-compose version not lower 1.29.2, or docker desktop version not
   lower 3.5.2
3. Prepare composeUp SLURM configuration. Follow to Gradle tab (right upper part of IDE window),
   choose Tasks -> docker -> composeUp, right click on composeUp, select "Modify Run Configuration".
   Choose name for your configuration (for example composeUp-SLURM). Then move down in this window
   and click on "Edit environment variables" button. In the new window add environment variable with
   the name TARGET_GRID_ENGINE and value SLURM. Save all changes.
4. Choose your saved configuration in IDEA's "Select Run/Debug Configuration" window.
5. Run composeUp task with your configuration, under the hood it will clean project, re-build jar
   file, build docker image and finally run a container with application.
6. After execution of composeUp task in the "Services" tab
   (row of tabs in the bottom line of IDEA window) in left down part of window you will find item "
   Docker" and run it. Then choose Docker -> Docker-compose grid-engine-api -> slurmctld -> slurmctld. Click on
   it, and you will see the sub-tab "Log" containing the log-output of running application. You need
   to find the line that contains "Listening for transport dt_socket at address: 5005". To the right
   of this line you will see the link "Attach debugger" (this link seems quite pale). Follow this
   link and debug session will start. Use IDEA Debugger to debug a containerized application.
7. When you`re done with all manipulations, go to the Gradle tab and find Tasks -> docker ->
   composeDown. Follow instructions described in the steps 3 and 4, but for composeDown task and
   create your composeDown SLURM configuration. Run this task with this configuration to shut down
   all containers.    
