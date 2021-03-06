package absolut.acc;

import absolut.can.CanReader;
import java.io.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Author: Hanna Carlsson, Peter Gärdenäs, Erik Magnusson, Elina Olsson.
 * Creates a server which revices data from the image detection and instructions from
 * the app.
 */

public class MopedServer {

    private static ACC acc;

	public static void init(ACC acc) {
	    MopedServer.acc = acc;
		//Copied from some stack overflow thread.
	    try {
	    	HttpServer server = HttpServer.create(new InetSocketAddress(9090), 0);
		    server.createContext("/response", new ImageHandler());
            server.createContext("/app", new AppHandler());
		    server.setExecutor(null); // creates a default executor
		    server.start();
		    System.out.println("Server up");
	    } catch (RuntimeException | IOException e) {
	    	e.printStackTrace();
	    }
    }

	//Receives a post request, handles it and sends a response.
	static class ImageHandler implements HttpHandler {

		//Saves the previous deviation of the red dot from image center
		private double prevOffset;

		//If a red dot is detected, the method steers the car
		public void handle(HttpExchange t) throws IOException {
	        t.sendResponseHeaders(200, 1);
	        String message = getMessage(t.getRequestBody()); //Gets the message from the camera, false if no red circle was found, and a number ((-100) - 100) if red circle is found
	        t.close();
	        if (!message.equals("false")) {
	        	double steerValue = 0;
	        	double lowPercentage = 0.4; // Percentage used to steer the car in small turns
	        	double highPercentage = 0.9; // Percentage used to steer the car in sharp turns
	        	double offset = Double.parseDouble(message); //The deviation of the red dot from image center
	        	if (isSharpTurn(offset)) {
	        		steerValue = offset * highPercentage;
				} else {
	        		steerValue = offset * lowPercentage;
				}
				int steerValueTmp = (int) Math.floor(steerValue);
	        	try {
	        		prevOffset = offset;
					CanReader.getInstance().sendSteering((byte) steerValueTmp); //Sends steer value to the can bus
				} catch (Exception e) {
	        		e.printStackTrace();
				}
			}
	    }

	    //Returns true when the turn is sharp
	    private boolean isSharpTurn(double offset) {
			int limitForSharpTurn = 50;
			return ((((offset >= 0 && prevOffset >= 0) || (offset < 0 && prevOffset < 0))
					&& Math.abs(offset - prevOffset) > limitForSharpTurn) || (((offset >= 0 && prevOffset < 0) ||
					(offset < 0 && prevOffset >= 0)) &&
					((Math.abs(offset) + Math.abs(prevOffset)) > limitForSharpTurn)));
		}
	}

	//Receives an input stream, detects the message from the input stream and returns it
	public static String getMessage(InputStream is) {
		BufferedReader in = new BufferedReader(
				new InputStreamReader(is, Charset.forName("UTF-8")));
		String inputLine;
		StringBuffer response = new StringBuffer();
		try {
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String temp = response.toString();
		return temp.substring(temp.indexOf("startM") + "startM".length(), temp.indexOf("endM"));
	}

    //Receives post request from app and handles it
    //@author Erik Magnusson
    private static class AppHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String message = getMessage(t.getRequestBody());
            if (message.charAt(0) == 'P') {
                System.out.println("Platoon " + (message.charAt(1) == 'T'));
//				setPlatoon((message.charAt(1) == 'T'))

            } else if (message.charAt(0) == 'A') {
				acc.setAccEnabled((message.charAt(1) == 'T'));
            } else if (message.charAt(0) == 'S') {
//				runMoped();
			}
            //Has to send a response, otherwise the app prints an error
            t.sendResponseHeaders(200, 1);
            t.close();
        }
    }
    //Work in progress..
    private static void runMoped() {
		System.out.println("Huumm");
		ProcessBuilder pb = new ProcessBuilder("../run.sh", "192.168.137.1");
		System.out.println(pb.directory().exists());
		System.out.println(pb.directory().getAbsolutePath());
		if (true) return;
		try {
			Process p = pb.start();
			Thread.sleep(5000l);
			p.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
