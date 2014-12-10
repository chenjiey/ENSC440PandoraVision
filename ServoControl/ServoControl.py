"""
ServoControl.py 

Description: 
    
    ServoControl.py is a server that controls the servo motor by using a 
        relative position

    ServoControl.py waits for a relative angle on the localhost (IP: 127.0.0.1) 
        on port 8000. The Event Algorithm is the Algorithm that occurs upon data
        arriving on port 8000.

    Event Algorithm:
    ----------------------------------------------------------------------------
    When data arrives on port 8000, verify that the data is a number.
        If the data is not a number, print an error message
        If the new position is outside the servo motor boundaries, 
            print error message
        Move the servo motor to the new position
        Update the current position

"""

import socket  # socket is the socket library that allows for commuication
import os
import time

# HOST = '' tells python we are going to look on all available addresses. 
# Looking at all addresses implies that python looks at the localhost ip address
# (127.0.0.1) and the address provided by a DHCP (192.168.X.Y) 

HOST = ''
PORT = int(os.environ.get("PORT", 8000))  # identifes what data we want

WHOAMI = 'pitch'  # Identifies which servo motor

# Initial Camera position upon a client successfully connecting to the server
INITIAL_POSITION = 90

MAX_ANGLE = 180  # The maximum angle that a servo motor can turn
MIN_ANGLE = 0  # The minimum angle that a servo motor can turn 

MIN_RECV_ANGLE = -180  # The minimum angle possible received from the client
DECIMAL_PLACES = 1  # The maximum decimal places for an angle

# COORD_SYS Makes the system based on relative or absolute coordinates
RELATIVE = 1
ABSOLUTE = 0

COORD_SYS = ABSOLUTE

TEST = 1
if TEST:
    PWM_STR = './'
else:
    PWM_STR = '/sys/class/rpi-pwm/pwm0/'

RASBIAN, OCCIDENTALIS = (0, 1)

# Sets the behavior of the script
__DISTRIBUTION__ = RASBIAN

if __DISTRIBUTION__ == RASBIAN:
    import RPIO.PWM as PWM
    PIN = 17


def get_map(angle_min, angle_max, total, offset=0):
    
    angles = range(offset, offset + total)
    pulses = range(angle_min, angle_max, (angle_max - angle_min)/total)

    pulses_new = [] 
    for item in pulses:
        res = item / 10.0
        pulses_new.append(int(round(res)) * 10)

    return dict(zip(angles, pulses_new))


def move_servo(angle):
    """
    Rashika! Here is where you would insert your code!

    Currently this prints the position where you want to move to
    """    
    print angle
    set_servo(angle)
    return


def access_property(prop, value):
    """Accesses a property of the PWM on the RPi"""
    prop = ''.join([PWM_STR, prop])
    try:
        with open(prop, 'w') as f:
            f.write(value)
    except:
        print("ERROR: FAILED TO WRITE %s TO: %s " % value, prop)
    return


def set_servo(angle):
    """Sets the servo motor to the correct orientation"""
    access_property("servo", str(angle))
    return


def main(connection, client_address, current_position, servo):
    """
    The main function waits for data to arrive on port 8000 and then calls
    move_servo(angle)

    When data arrives on port 8000, verify that the data is a number.
        If the data is not a number, print an error message
        If the new position is outside the servo motor boundaries, 
            print error message
        Move the servo motor to the new position
        Update the current position
   
    """
    if __DISTRIBUTION__ == RASBIAN:
        servo.set_servo(PIN, 1500)
    print "Connected to:", client_address

    # ------------------------- Event Algorithm --------------------------------
    try:
        while True:

            # Wait to receive data from a client, proceed when data has arrived
            recv_angle = connection.recv(1024)

            if recv_angle.lower() == 'whoami':
                connection.send(WHOAMI)
                break

            # If close is received terminate the server. Testing purposes only
            if recv_angle.lower() == 'close':
                break

            # Close the socket but wait for a new connection from a client
            if not recv_angle:
                break  # from while loop

            if not is_number(recv_angle):
                print "ERROR: DID NOT RECEIVE A NUMBER: %s " % recv_angle
                continue  # skip the rest of the algorithm

            # 20 decimal places do not make sense so rounding occurs here.
            if COORD_SYS == RELATIVE:
                new_position = current_position + round(float(recv_angle), DECIMAL_PLACES)
            else:
                new_position = round(float(recv_angle), DECIMAL_PLACES)
            
            if not (MIN_ANGLE < new_position < MAX_ANGLE):
                print "ERROR: NEW POSITION OUT OF BOUNDS"
                
                # Probably should update current position to Min position and Max
                # position
                
                continue
            
            print "Moving camera by: %s degrees" % recv_angle

            if __DISTRIBUTION__ == RASBIAN:

                # TODO(Jeremy 2014-12-08): Determing how the angle maps to the pulse
                #        with stored in current_position

                # current_position = 1500  # micro seconds

                servo.set_servo(PIN, R[current_position])
            else:
                move_servo(new_position)

            current_position = new_position
            print "current_position: %s" % current_position
    except Exception, e:
        print "Failed to do stuff"
    finally:
        # servo.stop_servo(PIN)
        connection.close()

    return recv_angle


def is_number(data):
    """Returns True if the number is True"""
    try:
        float(data)
        return True
    except ValueError:
        return False


if __name__ == '__main__':

    R = {}
    R1 = get_map(580, 900, 45)
    R2 = get_map(900, 1500, 45, 45)
    R3 = get_map(1500, 2100, 45, 90)
    R4 = get_map(2100, 2650, 45, 135)

    R.update(R1)
    R.update(R2)
    R.update(R3)
    R.update(R4)

    # access_property("delayed", "0")
    # access_property("mode", "servo")
    # access_property("servo_max", "180")
    
    # NOTE THAT CODE BEGINS HERE AND CALLS THE MAIN THREAD FROM WITHIN HERE
    while True:
        servo = PWM.Servo()

        # hostname = socket.gethostname()
        # IP = socket.gethostbyname(hostname)
        # print "SERVER: THEORETICAL IP ADDRESS: %s" % IP 
        print "SERVER: CREATING PORT: %s" % PORT
        
        # Create (instantiate) a socket and identify what type of socket to use
        #
        #   AF_INET: Specifes Internet protocol V4 (IPv4)
        #
        #   socket.SOCK_STREAM:
        #       Use Full Duplex communication between endpoint 1 and endpoint 2 
        #       so communication can occur in either direction

        cnt = 0
        while cnt < 10:
            try:
                sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

                # Bind configures the socket to the host address and the port number
                # begins watching port 8000
                
                sock.bind((HOST, PORT))
                cnt += 1

            except Exception, e:
                time.sleep(1)
                print "Failed to restart server"
                if cnt >= 9:
                    result = 'close'

        # Waits for single client to connect
        sock.listen(1)

        # Accept a connection from a client
        connection, client_address = sock.accept() 

        # access_property("active", "1")
        print "Returning the Camera back to initial position"
        current_position = INITIAL_POSITION
        # move servo
        # move_servo(current_position)
        
        # Enter the main loop
        result = main(connection, client_address, current_position, servo)
        print "connection has stopped"
        
        servo.stop_servo()
        del sock, connection, client_address
        if result == 'close':
            break

        # access_property("active", "0")

    print "Closing Server"
