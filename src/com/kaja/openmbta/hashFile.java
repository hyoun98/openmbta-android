package com.kaja.openmbta;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import android.content.Context;
import android.util.Log; 

public class hashFile {
	
	
	  public  void doSave(HashMap hm_, String bmFileName_, Context ctx_) {

		  	// serializes a hashmap to file
	  
	  
	        try {     	
	        	
	            System.out.println("Creating File/Object output stream...");
	           
	            //FileOutputStream fileOut = new FileOutputStream("open.pref", Context.MODE_PRIVATE);
	            FileOutputStream fileOut = ctx_.openFileOutput(bmFileName_, Context.MODE_PRIVATE);
	            ObjectOutputStream out = new ObjectOutputStream(fileOut);

	            System.out.println("Writing Hashtable Object...");
	            out.writeObject(hm_);

	            System.out.println("Closing all output streams...\n");
	            out.close();
	            fileOut.close();
	           
	        } catch(FileNotFoundException e) {
	        	Log.e("ERROR", "FNF Output" + e.toString());
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	  
	  public  HashMap doLoad(String bmFileName_, Context ctx_) {
		  		HashMap<String, Integer> h = null;


	        try {

	            System.out.println("Creating File/Object input stream...");
	           
	           //FileInputStream fileIn = new FileInputStream("open.pref");
	           FileInputStream fileIn = ctx_.openFileInput(bmFileName_);
	            ObjectInputStream in = new ObjectInputStream(fileIn);

	            System.out.println("Loading Hashtable Object...");
	            h = (HashMap)in.readObject();

	            System.out.println("Closing all input streams...\n");
	            in.close();
	            fileIn.close();
	           
	            
	        } catch (ClassNotFoundException e) {
	            e.printStackTrace();
	        } catch(FileNotFoundException e) {
	        	Log.e("ERROR", "FNF" + e.toString());
	            e.printStackTrace();
	           
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

         
	        return h;
	    }

	
}
