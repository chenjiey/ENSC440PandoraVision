REM # I am writing this script to quickly allow you to get a test environment
REM # to send back and forth characters
REM # Python is required to be able to run the script

set PORT=4444

start python ./ServoControl.py
python ./Client.py
