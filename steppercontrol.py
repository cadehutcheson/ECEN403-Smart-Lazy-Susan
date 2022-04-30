import RPi.GPIO as GPIO
import time

#physical pins # for stepper motor In1-4
ControlPin = [7,11,13,16]

#setup pins for output
def setup():
    GPIO.setmode(GPIO.BOARD)

    for pin in ControlPin:
        GPIO.setup(pin, GPIO.OUT)
        GPIO.output(pin, False)
        
                  
#clockwise rotation function
def StepperCW(rnum):
    step_seq_num = 0
    rot_speed = 0.004
    rots = 0
    rot_dir = 1
	#input stepper motor ABCD sequence
    seq = [[1,0,0,0],
           [1,1,0,0],
           [0,1,0,0],
           [0,1,1,0],
           [0,0,1,0],
           [0,0,1,1],
           [0,0,0,1],
           [1,0,0,1],
        ]
    #4096 = 1 rotation-- if input value is 1, quarter rotation is done, half rotation for 2
    if rnum == 1: rots = 4096/4
    elif rnum == 2: rots = 4096/2
    
	#loop through seq until rotation complete, each loop lasts rot_speed time
	#total time = rots*rot_spd
    for i in range(0,int(rots+1)):
        for pin in range(0,4):
            Pattern_Pin = ControlPin[pin]
            if seq[step_seq_num][pin] == 1:
                GPIO.output(Pattern_Pin, True)
            else:
                GPIO.output(Pattern_Pin, False)
        #increment or decrement step_seq_num based on CW or CCW
        step_seq_num += rot_dir
        if(step_seq_num >= 8):
            step_seq_num = 0
        elif step_seq_num < 0:
            step_seq_num = 7
        
        time.sleep(rot_speed)
    
#Counter-Clockwise rotation function
def StepperCCW(rnum):
    step_seq_num = 0
    rot_speed = 0.004
    rots = 0
    rot_dir = -1

    seq = [[1,0,0,0],
           [1,1,0,0],
           [0,1,0,0],
           [0,1,1,0],
           [0,0,1,0],
           [0,0,1,1],
           [0,0,0,1],
           [1,0,0,1],
        ]
    
    if rnum == 1: rots = 4096/4
    elif rnum == 2:rots = 4096/2
    
    for i in range(0,int(rots+1)):
        for pin in range(0,4):
            Pattern_Pin = ControlPin[pin]
            if seq[step_seq_num][pin] == 1:
                GPIO.output(Pattern_Pin, True)
            else:
                GPIO.output(Pattern_Pin, False)
        
        step_seq_num += rot_dir
        if(step_seq_num >= 8):
            step_seq_num = 0
        elif step_seq_num < 0:
            step_seq_num = 7
        
        time.sleep(rot_speed)
    
    
#def StepperStop():
    

if __name__ == '__main__':
    setup()


