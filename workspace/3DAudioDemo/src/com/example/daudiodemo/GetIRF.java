package com.example.daudiodemo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import android.util.FloatMath;

public class GetIRF {

	//bounds on elevation values
	static final int MIN_ELEV = -40;
	static final int MAX_ELEV = 90;
	static final int ELEV_INC = 10;
	static final int N_ELEV = (((MAX_ELEV - MIN_ELEV) / ELEV_INC) + 1);

	//bounds on azimuth values
	static final int MIN_AZIM = -180;
	static final int MAX_AZIM = 180;

	//number of time samples
	static final int time_samples = 128;

	// Other variables necessary for getIrf
	//determine if a flip is necessary
	static boolean flip;

	//variables to keep track of new information
	static int cur_el_index;
	static int cur_az_index;
	static boolean cur_flip_flag;
	static int last_el_index;
	static int last_az_index;
	static boolean last_flip_flag;
	static double cur_atten;
	static double last_atten;
	static boolean changed;

	//number of azimuth data points per elevation (-40 to 90, increments of 10)
	public static int[] elev_data = {56, 60, 72, 72, 72, 72, 72, 60, 56, 45, 36, 24, 12, 1};

	/***************************************************
	 * Why are you creating your own class and returning it for an array
	 * of [2][128]? There's no reason to rebuild code that already exists
	 * You could simply make a 2D array that is 14 wide and 72*2 high
	 ****************************************************/
	//14 different elevations at 10 degree increments (-40 to 90)
	//72 different azimuth measurements MAXIMUM (some are less)
	public HACKED_SAMPLES[][] irf_data;

	// This contains the sine wave that we are hacking
	public final int hacked_time_samples = 150;
	/*public final int front_sine = time_samples;
	public final int end_sine = hacked_time_samples+time_samples*2 - time_samples;*/
	public float[] sineWave = new float[hacked_time_samples+time_samples];

	/******************
	 * I don't think you need these parameters
	 * @param azimuth
	 * @param elevation
	******************* */
	GetIRF () {
		irf_data = new HACKED_SAMPLES[14][72];

		// create sine wave
		final float frequency = 441/2;
        float increment = (float)(2*Math.PI) * frequency / 44100; // angular increment for each sample
        float angle = 0;
        
		for( int i=0; i < sineWave.length; i++ )
        {
           sineWave[i] = (float)FloatMath.sin(angle);
           angle += increment;
        }

		read_irfs();
	}


	/////////////////////////////////
	// Helper functions for getIrf //
	/////////////////////////////////
	//Reads in all IRFs by looping through all possible elevations and azimuths
	/*****************
	 * I don't think you need these parameters
	 * @param az_index
	 * @param el_index
	 **********************/
	public void read_irfs()
	{
		int nfaz;

		//loops through # of different indices (N_ELEV)
		for (int el_index = 0; el_index < N_ELEV; el_index++) 
		{
			//uses elev_data to find how many associated azimuths there are
			nfaz = (elev_data[el_index] / 2) + 1;

			//loops through all azimuths
			for (int az_index = 0; az_index < nfaz; az_index++) 
			{
				//actually load IRFs
				read_irf(el_index, az_index);
			}
		}
	}

	//Uses bufferedReader to read in IRF data line by line based on elevation and azimuth
	public void read_irf(int el_index, int az_index)
	{		
		String filename = irf_name(el_index, az_index);

		//line number
		int linecount = 0;
		//line number mod # of time samples (128 in this case)
		int linecountmod = 0;
		//time sample in double format
		double sample;
		IRF_DATUM data = new IRF_DATUM();

		InputStream in = this.getClass().getClassLoader().getResourceAsStream(filename);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

		String line;

        try 
        {
        	
            //Construct the BufferedReader object
         //   bufferedReader = new BufferedReader(new FileReader(filename));
            
          //  String line = null;
            
         //   while ((line = bufferedReader.readLine()) != null) 
        	
        	line = bufferedReader.readLine();
        	
        	while (line != null)
            {	
            	sample = Double.parseDouble(line);
            	
            	if(irf_data[el_index][az_index] == null)
        		{
        			irf_data[el_index][az_index] = new HACKED_SAMPLES();
        			//irf_data[el_index][az_index].left = new float[]
        		}
            	
            	//if time_samples > 128, then it is the right side
            	if(linecount >= time_samples)
            	{
            		linecountmod = linecount % 128;
            		
            		data.right[linecountmod] = (float) sample;
            	}
            	
            	//if time samples <= 128, it is on the left side
            	else if(linecount > -1)
            	{		
            		//System.out.println("irf data out: " + irf_data[el_index][az_index].left[linecount]);
            		
            		data.left[linecount] = (float) sample;
            	}
            	
            	linecount++;
            	
        		line = bufferedReader.readLine();
            	
            }
        } 
        catch (FileNotFoundException ex) 
        {
            ex.printStackTrace();
        }
        catch (IOException ex) 
        {
            ex.printStackTrace();
        } 
        finally 
        {
            //Close the BufferedReader
            try 
            {
                if (bufferedReader != null)
                {
                	 bufferedReader.close();
                }
            }        
            catch (IOException ex) 
            {
                ex.printStackTrace();
            }
        }
        
        // Now we are going to modify the data so that it contains our desired hacked values
        
        irf_data[el_index][az_index].left = ConvolveHack.convolve(sineWave,data.left);
        irf_data[el_index][az_index].right = ConvolveHack.convolve(sineWave,data.right);
	}

	//returns IRF pathname specified by indices
	public static String irf_name(int el_index, int az_index)
	{
		int elev = index_to_elev(el_index);
		int azim = index_to_azim(el_index, az_index);

		//pathname based on elev and azim
		// TODO: CHANGE THIS PATH TO SUIT CURRENT MACHINE!
		// Better yet, change it to a relative path!
		String hrtfname;
		if (elev < 0) {
			int mag = Math.abs(elev);
			hrtfname = String.format("res/raw/h_%de%03da.txt", mag, azim);
		}
		else
			hrtfname = String.format("raw/h%de%03da.txt", elev, azim);
		return hrtfname;
	}

	public HACKED_SAMPLES get_irf(double elev, double azim)
	{
		//System.out.println("Entered get_irf");
		/*
		 * Returns a 2D array with the left channel IRFs in [0] row and
		 * the right channel IRFs in the [1] row. Uses a special datatype, IRF_DATUM,
		 * to fetch these two channels from an already-2D array.
		 */
		//used to store IRF information
		HACKED_SAMPLES hd;

		//get elevation and azimuth indices (also get in correct range)
		cur_el_index = get_el_index(elev);
		//System.out.format("cur_el_index is %d for an elev of %d%n", cur_el_index, elev);
		cur_az_index = get_az_index(elev, azim);
		//System.out.format("cur_az_index is %d for an azim of %d%n", cur_az_index, azim);

		//if anything has changed
		if (cur_el_index != last_el_index || cur_az_index != last_az_index || cur_flip_flag != last_flip_flag)
		{
			changed = true;
		}

		//update indices
		last_el_index = cur_el_index;
		last_az_index = cur_az_index;
		last_flip_flag = cur_flip_flag;

		//System.out.println("About to access irf_data");
		//Get data and flip channels if necessary.
		hd = irf_data[cur_el_index][cur_az_index];

		//System.out.println("Retrieved IRF_DATUM from irf_data");
		//hd.left = irf_data[cur_el_index][cur_az_index].left;
		//hd.right = irf_data[cur_el_index][cur_az_index].right;

		float[] temp;
		if (cur_flip_flag) 
		{
			temp = hd.left;
			hd.left = hd.right;
			hd.right = temp;
		}

		return hd;
	}

	/*
	 * For a given elevation and azimuth in degrees, return the
	 * indices for the proper HRTF. *p_flip will be set TRUE if
	 * left and right channels need to be flipped.
	 */
	public static int get_az_index(double elev, double azim)
	{

		int naz, nfaz;
		int el_index;
		int az_index;
		el_index = get_el_index(elev);
		naz = elev_data[el_index];
		nfaz = (elev_data[el_index] / 2) + 1;
		/*
		 * Coerce azimuth into legal range and calculate
		 * flip if any.
		 */
		azim = azim % 360.0;
		if (azim < 0) azim += 360;
		if (azim > 180) {
			azim = 360 - azim;
			flip = true;
		}
		else flip = false;

		/*
		 * Now 0 <= azim <= 180. Calculate index and clip to
		 * legal range just to be sure.
		 */
		az_index = round(azim / (360.0 / naz));
		if (az_index <= 0) az_index = 0;
		else if (az_index >= nfaz) az_index = nfaz - 1;

		return az_index;
	}

	//get elevation index from the elevation
	public static int get_el_index(double elev)
	{
		int el_index;
		el_index = round((elev - MIN_ELEV) / ELEV_INC);
		if (el_index < 0) el_index = 0;
		else if (el_index >= N_ELEV) el_index = N_ELEV - 1;
		return el_index;
	}

	//returns azimuth based on az index
	static int index_to_azim(int el_index, int az_index)
	{
		return round(az_index * 360.0 / elev_data[el_index]);
	}

	//returns elevation based on elevation index
	static int index_to_elev(int el_index)
	{
		return MIN_ELEV + el_index * ELEV_INC;
	}

	//round the number
	public static int round(double val)
	{
		return (int) ((val > 0) ? val + 0.5 : val - 0.5);
	}
}