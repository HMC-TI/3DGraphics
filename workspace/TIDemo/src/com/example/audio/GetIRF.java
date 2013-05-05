package com.example.audio;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import android.util.FloatMath;

public class GetIRF {

	// bounds on elevation values
	static final int MIN_ELEV = -40;
	static final int MAX_ELEV = 90;
	static final int ELEV_INC = 10;
	static final int N_ELEV = (((MAX_ELEV - MIN_ELEV) / ELEV_INC) + 1);

	// bounds on azimuth values
	static final int MIN_AZIM = -180;
	static final int MAX_AZIM = 180;

	// variables to keep track of new information
	static int cur_el_index;
	static int cur_az_index;
	static boolean cur_flip_flag;

	// number of azimuth data points per elevation (-40 to 90, increments of 10)
	public static int[] elev_data = { 56, 60, 72, 72, 72, 72, 72, 60, 56, 45,
			36, 24, 12, 1 };

	// 14 different elevations at 10 degree increments (-40 to 90)
	// 72 different azimuth measurements MAXIMUM (some are less)
	public HACKED_SAMPLES[][] irf_data;

	GetIRF() {
		irf_data = new HACKED_SAMPLES[14][72];
		read_irfs();
	}

	/**
	 * Reads in all IRFs by looping through all possible elevations and azimuths
	 */
	public void read_irfs() {
		int nfaz;

		// loops through # of different indices (N_ELEV)
		for (int el_index = 0; el_index < N_ELEV; el_index++) {
			// uses elev_data to find how many associated azimuths there are
			nfaz = (elev_data[el_index] / 2) + 1;

			// loops through all azimuths
			for (int az_index = 0; az_index < nfaz; az_index++) {
				// actually load IRFs
				read_irf(el_index, az_index);
			}
		}
	}

	/**
	 * Uses bufferedReader to read in IRF data line by line based on elevation
	 * and azimuth
	 * 
	 * @param el_index
	 *            - the elevation index to read in values for (between 0 and 13)
	 * @param az_index
	 *            - the azimuth index to read in values for (varies based on
	 *            elevation)
	 */
	public void read_irf(int el_index, int az_index) {
		String filename = irf_name(el_index, az_index);

		// line number
		int linecount = 0;
		// line number mod # of time samples (128 in this case)
		int linecountmod = 0;
		// time sample in double format
		double sample;

		InputStream in = this.getClass().getClassLoader()
				.getResourceAsStream(filename);
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(in));

		String line;

		try {
			line = bufferedReader.readLine();

			while (line != null) {
				sample = Double.parseDouble(line);

				if (irf_data[el_index][az_index] == null) {
					irf_data[el_index][az_index] = new HACKED_SAMPLES();
				}

				// if it is the right side
				if (linecount >= HACKED_SAMPLES.sample_size) {
					linecountmod = linecount % HACKED_SAMPLES.sample_size;

					irf_data[el_index][az_index].right[linecountmod] = (float) sample;
				}

				// if it is on the left side
				else if (linecount > -1) {
					irf_data[el_index][az_index].left[linecount] = (float) sample;
				}

				linecount++;

				line = bufferedReader.readLine();

			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			// Close the BufferedReader
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * returns IRF pathname specified by indices
	 */
	public static String irf_name(int el_index, int az_index) {
		int elev = index_to_elev(el_index);
		int azim = index_to_azim(el_index, az_index);

		String hrtfname;
		if (elev < 0) {
			int mag = Math.abs(elev);
			hrtfname = String.format("res/raw/h_%de%03da.txt", mag, azim);
		} else
			hrtfname = String.format("res/raw/h%de%03da.txt", elev, azim);
		return hrtfname;
	}

	public HACKED_SAMPLES get_irf(double elev, double azim) {
		// Data to be returned
		HACKED_SAMPLES hd;

		// get elevation and azimuth indices (also get in correct range)
		cur_el_index = get_el_index(elev);
		cur_az_index = get_az_index(elev, azim);

		// Get data and flip channels if necessary.
		hd = irf_data[cur_el_index][cur_az_index];

		return hd;
	}

	/**
	 * For a given elevation and azimuth in degrees, return the indices for the
	 * proper HRTF. 'cur_flip_flag will be set TRUE if left and right channels
	 * need to be flipped.
	 */
	public static int get_az_index(double elev, double azim) {

		int naz, nfaz;
		int el_index;
		int az_index;
		el_index = get_el_index(elev);
		naz = elev_data[el_index];
		nfaz = (elev_data[el_index] / 2) + 1;
		/*
		 * Coerce azimuth into legal range and calculate flip if any.
		 */
		azim = azim % 360.0;
		if (azim < 0)
			azim += 360;
		if (azim > 180) {
			azim = 360 - azim;
			cur_flip_flag = true;
		} else
			cur_flip_flag = false;

		/*
		 * Now 0 <= azim <= 180. Calculate index and clip to legal range just to
		 * be sure.
		 */
		az_index = round(azim / (360.0 / naz));
		if (az_index <= 0)
			az_index = 0;
		else if (az_index >= nfaz)
			az_index = nfaz - 1;

		return az_index;
	}

	// get elevation index from the elevation
	public static int get_el_index(double elev) {
		int el_index;
		el_index = round((elev - MIN_ELEV) / ELEV_INC);
		if (el_index < 0)
			el_index = 0;
		else if (el_index >= N_ELEV)
			el_index = N_ELEV - 1;
		return el_index;
	}

	// returns azimuth based on az index
	static int index_to_azim(int el_index, int az_index) {
		return round(az_index * 360.0 / elev_data[el_index]);
	}

	// returns elevation based on elevation index
	static int index_to_elev(int el_index) {
		return MIN_ELEV + el_index * ELEV_INC;
	}

	// round the number
	public static int round(double val) {
		return (int) ((val > 0) ? val + 0.5 : val - 0.5);
	}
}
