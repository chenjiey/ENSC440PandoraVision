# Echo client program
import socket
import time

HOST = '127.0.0.1'    # The remote host
PORT = 8000           # The same port as used by the server

# WAIT_DURATION_IN_SECONDS = 1
WAIT_DURATION_IN_SECONDS = 0.5


def send_characters(s):
    """
    As a test for the server we send numbers using this
    Client Application.
    """
    chars = raw_input("What do you wish to send:\n > ")
    
    # allows you to stop the client by typing 'stop'
    if chars.lower() in ['stop']:
        result = 0

    # allows you to stop the client by pressing enter w/o any chars
    elif chars == '':
        result = 0
    else:
        s.send(chars)
        result = chars

    return result


def test_case_one_second_interval(sock):
    """
    I've designed a simple test case that sends numbers 0, 180 to the
    server which the server prints it out
    """
    # Create a list with [10, 10, ..., 10, -10, -10, ..., -10 ]
    test_vals = [10] * 9 + [-10] * 9

    for number in test_vals:
        time.sleep(WAIT_DURATION_IN_SECONDS)
        sock.send(str(number))


def main():
    """Establishes a connection to a server so that we can test the control"""
    # socket connection matching server protocol and socket type
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    # connects the client to the server
    sock.connect((HOST, PORT))

    test_case_one_second_interval(sock)

    print("To stop sending characters to the server enter 'stop'")
    while True:
        sent = send_characters(sock)
        if not sent:
            break
    shutdown = raw_input("Do You Wish to shutdown the server? Y/n\n > ")
    
    if shutdown.upper() == 'Y':
        sock.send('close')
    print "Closing Client Application!"
    sock.close()
    return

if __name__ == '__main__':
    main()
