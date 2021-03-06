import requests
import time
import os
import picamera
import sys

#A modification of OptiposRPiClient.py

#Post image to java server
def postImage(session, url):
    """
    Post an image to the server.
    """
    print("Posting image...")
    try:
        response = session.post(url, files = {"file": ("", open("/dev/shm/optiposimage.jpg", "rb"))}).text
    except Exception as e:
        print(e)
        response = "Connection broken"
    return response

def main():
    print("Camera starting")
    #The script can be started with an argument contaning adress of the Java stream.
    if (len(sys.argv) == 2):
        server = 'http://' +  sys.argv[1] + ':8080/processimage'
    else:
        server = 'http://192.168.43.246:8080/processimage'

    print(server)
    camera = picamera.PiCamera()
    resolution = 972
    camera.resolution = (resolution, resolution)
    camera.iso = 800
    camera.meter_mode = "backlit"

    with requests.Session() as session:

        time.sleep(2)

        response = None
        # Construct a stream to hold image data temporarily (we could write it directly to connection but in this
        # case we want to find out the size of each capture first to keep the protocol simple)
       # if True:
        for _ in camera.capture_continuous("/dev/shm/optiposimage.jpg", 'jpeg', use_video_port = True, quality = 10):
            try:
                # Get the start time, to be able to calculate response time
                start = time.time()
                response = postImage(session, server)
               # response2 = postImage(session,'http://192.168.137.1:8080/processimage')
                end = time.time()
                print('Received [' + response + '] after ' + str(end - start) + ' s')
            except:
                pass


main()
