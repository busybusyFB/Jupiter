package rpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class RPCHelper {
	public static void writeJsonArray(HttpServletResponse response, JSONArray array) throws IOException {
		response.setContentType("application/json");
		response.setHeader("Access-Control-Allow-Origin", "*");
		PrintWriter out = response.getWriter();
		out.print(array);
		out.close();
	}
	
	public static void writeJsonObject(HttpServletResponse response, JSONObject obj) throws IOException {
		response.setContentType("application/json");
		response.setHeader("Access-Control-Allow-Origin", "*");
		PrintWriter out = response.getWriter();
		out.print(obj);
		out.close();
	}
	
	public static JSONObject readJSONObject(HttpServletRequest request) {
		try {
			BufferedReader reader = request.getReader();
			StringBuilder sb = new StringBuilder();
			String line;
			while((line = reader.readLine()) != null) {
				sb.append(line);
			}
			return new JSONObject(sb.toString());
			
		} catch (JSONException e) {
				e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new JSONObject();
	}
	
}
