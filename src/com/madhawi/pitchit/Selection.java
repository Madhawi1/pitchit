package com.madhawi.pitchit;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class Selection extends Activity{
	 private MediaPlayer player ;
	 private MediaRecorder mRecorder;
	 private File audiofile;
	 
	 //variables for generating and playing the tone
	 private final int duration = 3; // seconds
	 private final int sampleRate = 8000;
	 private final int numSamples = duration * sampleRate;
	 private final double sample[] = new double[numSamples];
	 private double freqOfTone = 261.63; // hz
	 private final byte generatedSnd[] = new byte[2 * numSamples];
	 private Thread recordingThread = null;
	
	 //variables for tracking pitch
	 private boolean recording=false;
	 AudioRecord recorder;
	 short[] audioData;
	 int bufferSize;
	 int samplerate =8000;
	 private Complex[] complexData;

	 Handler handler = new Handler();
	 
	 public void trackPitch(){
	 
     Toast.makeText(Selection.this, "Recording voice",Toast.LENGTH_LONG).show();
     //get the buffer size to use with this audio record
	 bufferSize = AudioRecord.getMinBufferSize(samplerate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)*3;
	 int bufferSize1=bufferSize ; 
	 bufferSize =(int)Math.pow(2, Integer.toBinaryString(bufferSize).length());
		//instantiate the AudioRecorder
	 recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,samplerate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,bufferSize); 
	 	
	 recording = true; //variable to use start or stop recording
	 audioData = new short [bufferSize]; //short array that pcm data is put into.

	 }

	 public void analyze(){
		 complexData = new Complex[audioData.length];
			
		 for (int i = 0; i < complexData.length; i++) {
		     complexData[i] = new Complex(audioData[i], 0);
		 }
		 
		 Complex[] fftResult = FFT.fft(complexData);
		 Toast.makeText(Selection.this, "fft1  "+fftResult[0],Toast.LENGTH_LONG).show();
		 Toast.makeText(Selection.this, "fft2  "+fftResult[1],Toast.LENGTH_LONG).show();
		 Toast.makeText(Selection.this, "fft3  "+fftResult[10],Toast.LENGTH_LONG).show();
		 Toast.makeText(Selection.this, "fftlength "+fftResult.length,Toast.LENGTH_LONG).show();
				 
		 
	 }
	 public void startRecording1(){
		 trackPitch();
		 recorder.startRecording();
		 final Thread recordingThread = new Thread(new Runnable() {
	            public void run() {
	                
	                    	while(recording){
	           	       		 recorder.read(audioData,0,bufferSize);
	           	       		}
	                    }
	            }
	        );
	        recordingThread.start();
		 
	 }
	 public void stopRecording1(){
		 if (null != recorder) {
		        recording = false;
		        Toast.makeText(Selection.this, "Done " ,Toast.LENGTH_LONG).show();
		    	
		        recorder.stop();
		        recorder.release();

		        recorder = null;
		        recordingThread = null;
		       
		        analyze();
		        
		 }
		 
	 }
	 private void setButtonHandlers() {
		    ((Button) findViewById(R.id.btnRecord)).setOnClickListener(btnClick);
		    ((Button) findViewById(R.id.btnStop)).setOnClickListener(btnClick);
		}

	 private View.OnClickListener btnClick = new View.OnClickListener() {
		    public void onClick(View v) {
		        switch (v.getId()) {
		        case R.id.btnRecord: {
		            enableButtons(true);
		            startRecording1();
		            break;
		        }
		        case R.id.btnStop: {
		            enableButtons(false);
		            stopRecording1();
		            break;
		        }
		        }
		    }
		};
	 private void enableButton(int id, boolean isEnable) {
		    ((Button) findViewById(id)).setEnabled(isEnable);
		}

		private void enableButtons(boolean isRecording) {
		    enableButton(R.id.btnRecord, !isRecording);
		    enableButton(R.id.btnStop, isRecording);
		    enableButton(R.id.btnPlay, !isRecording);
		}

		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);
		setButtonHandlers();
		

}
	//play the selected note
	public void play(View view){
		
		 final RadioGroup rgNotes = (RadioGroup) findViewById(R.id.rgNotes);
		
		 //get the selected note from the group of radio buttons
		 RadioButton selectRadio = (RadioButton) findViewById(rgNotes.getCheckedRadioButtonId());
         String note = selectRadio.getText().toString();
         
         //Show a toast "Playing note 'C,D, etc'" indicating that the note is playing
         Toast.makeText(Selection.this, "Playing note " + note,Toast.LENGTH_LONG).show();
         char noteC=note.charAt(0);
	 
         //Set the frequency of the selected note
         switch(noteC){
         	case 'C':
         		freqOfTone=261.63;
         		break;
         	case 'D':
         		freqOfTone=293.66;
         		break;
         	case 'E':
         		freqOfTone=329.63;
         		break;
         	case 'F':
         		freqOfTone=349.23;
         		break;
         	case 'G':
         		freqOfTone=392.00;
         		break;
         	case 'A':
         		freqOfTone=440;
         		break;
         	case 'B':
         		freqOfTone=493.88;
         		break;
	 }
	
	 
      final Thread thread = new Thread(new Runnable() {
      public void run() {
          genTone();
          handler.post(new Runnable() {

              public void run() {
                  playSound();
              }
          });
      }
  });
  thread.start();
 
	}
	
	//Generate the tone
	public void genTone(){
		
        //Fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
        }

        // Convert to 16 bit pcm sound array
        // Assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // Scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // In 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
    }

	public void playSound(){
        
		final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
													 sampleRate, AudioFormat.CHANNEL_OUT_MONO,
													 AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
													 AudioTrack.MODE_STATIC);
        											 audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.play();
    }


	
	
	//Record voice
	public void record(View view){
		//Show a toast "Recording Voice" indicating that recording has started
		Toast.makeText(Selection.this, "Recording",Toast.LENGTH_LONG).show();

		startRecording();
		    
		try {
			//Keep recording for 5 seconds
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		//Shows a toast "Done" indicating that recording has finished
		Toast.makeText(Selection.this, "Done",Toast.LENGTH_LONG).show();
		 
		stopRecording();
	}
	
	//Start recording voice
	private void startRecording() {
			
		 try {
			  //create a file with prefix 'sound' and suffix '.3gp'
		      audiofile = File.createTempFile("sound", ".3gp");
		    } catch (IOException e) {
		       return;
		    }
		 
		 	//Make a media recorder to record voice
		    mRecorder = new MediaRecorder();
		    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		    mRecorder.setOutputFile(audiofile.getAbsolutePath());
		    
		    try {
		    	//Prepares the recorder to start capturing voice
				mRecorder.prepare();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    //Start the recorder
		    mRecorder.start();
		  }
	
	//Stop recording voice	
	private void stopRecording() {
		
	    mRecorder.stop();
	    //Releases the resources associated with the recorder
	    mRecorder.release();
	    addRecordingToMediaLibrary();
	   
	}	 
	
	//Add the recorded file to the media library
	protected void addRecordingToMediaLibrary() {
		
	    ContentValues values = new ContentValues(4);
	    long current = System.currentTimeMillis();
	    values.put(MediaStore.Audio.Media.TITLE, "audio" + audiofile.getName());
	    values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
	    values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
	    values.put(MediaStore.Audio.Media.DATA, audiofile.getAbsolutePath());
	    ContentResolver contentResolver = getContentResolver();

	    Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	    Uri newUri = contentResolver.insert(base, values);

	    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri));
	    
	    //Shows a toast that gives the path of the added file
	    Toast.makeText(this, "Added File " + newUri, Toast.LENGTH_LONG).show();
	  }
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	
}
