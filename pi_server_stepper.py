from os import times_result
import steppercontrol
from socket import *
from time import ctime
import RPi.GPIO as GPIO

#call stepper motor setup function
steppercontrol.setup()

#5 possible commands to receive from app
motorCommands = ['I', 'D', '180', 'Clockwise', 'CounterCW']
HOST = ''
#port for listening
PORT = 21567
#max size for packet
BUFSIZE = 1024
ADDR = (HOST, PORT)

#create server side tcp socket to listen for incoming data from app
tcpServer = socket(AF_INET, SOCK_STREAM)
tcpServer.bind(ADDR)
tcpServer.listen(5)

#continuously run and listen for commands
while True:
    print('Getting connection')
	#get packet and address from received tcp packet
    tcpClient,addr = tcpServer.accept()
    print('Connection successfull from:', addr)

    try:
        while True:
            data = ''
			#separate data from received packet
            data = tcpClient.recv(BUFSIZE)
            if not data:
                break
			#find command that was sent, execute stepper motor accordingly
            if motorCommands[0] in str(data):
                steppercontrol.StepperCW(1)
                print("CW buttton pressed")
            if motorCommands[1] in str(data):
                steppercontrol.StepperCCW(1)
                print("CCW button pressed")
            if motorCommands[2] in str(data):
                steppercontrol.StepperCW(2)
                print("ROTATE 180")
            if motorCommands[3] in str(data):
                steppercontrol.StepperCW(1)
                print("ROTATE CW 90")
            if motorCommands[4] in str(data):
                steppercontrol.StepperCCW(1)
                print("ROTATE CCW 90")
            
    except KeyboardInterrupt:
        GPIO.cleanup()

tcpServer.close()
GPIO.cleanup()


