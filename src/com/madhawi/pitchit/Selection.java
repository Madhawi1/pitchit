package com.madhawi.pitchit;



import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Toast;

public class Selection extends Activity{
	 
	 //variables for generating and playing the tone
	 private final int duration = 3; // seconds
	 private final int sampleRate = 8000;
	 private final int numSamples = duration * sampleRate;
	 private final double sample[] = new double[numSamples];
	 private double freqOfTone = 261.63; // hz
	 private final byte generatedSnd[] = new byte[2 * numSamples];
	 private Thread recordingThread = null;
	 private boolean isPlaying=false;
	 private  String note=null;
	
	 //variables for tracking pitch
	 private boolean isRecording=false;
	 AudioRecord recorder;
	 short[] audioData;
	 int bufferSize;
	 int samplerate =8000;
	 private Complex[] complexData;
	 
	 
	 Handler handler = new Handler();
	 
	 //Analyze the audio data using fft class
	 public void analyze(){
		
		 int index=0;
		 double max=0;
		 double frequency=0;
		 complexData = new Complex[audioData.length];
			
		 for (int i = 0; i < complexData.length; i++) {
		     complexData[i] = new Complex(audioData[i], 0);
		 }
		 
		 Complex[] fftResult = FFT.fft(complexData);

		 
		 
		 //Define the minimum and maximum values of the range
		 int minValue=(int)254.258*fftResult.length/samplerate;
		 int maxValue=(int)555.29*fftResult.length/samplerate;
		 
		 for (int i=minValue;i<maxValue;i++){
			 double magnitude=fftResult[i].re()*fftResult[i].re()+fftResult[i].im()*fftResult[i].im();
			 if(magnitude>max){
				 max=magnitude;
				 index=i;
				 }
			 
		 }
		 //Get the frequency of the point within the range with maximum magnitude
		 frequency=((double)samplerate*index)/(double)fftResult.length;
		
		 makeFeedback(frequency);
		 Toast.makeText(Selection.this, "Your frequency:   "+frequency+"Hz  Expected:         "+freqOfTone + "Hz",Toast.LENGTH_LONG).show();
		 
	 }
	 
	  
	 //Display feedback to the user according to pitching level
	 public void makeFeedback(double voiceFrequency){
		 double difference=voiceFrequency-freqOfTone;
		 
		 RatingBar feedbackBar = (RatingBar) findViewById(R.id.feedbackBar);
		 LayerDrawable stars = (LayerDrawable) feedbackBar.getProgressDrawable();
		 stars.getDrawable(2).setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
		 
		 ProgressBar lowBar= (ProgressBar) findViewById(R.id.lowBar);
		 ProgressBar highBar= (ProgressBar) findViewById(R.id.highBar);
		 int differenceInt=(int)difference;
		
		 //display feedback in star(rating bar) and progress bars
		 if(difference<4 && difference>-4 ){
				feedbackBar.setRating(1);
			}
			else if(difference<0){
		
				lowBar.setProgress(-differenceInt);
			}
			else {
				highBar.setProgress(differenceInt);
			}

				 }
	 
	 //Get the note selected by user and set it as the note selected
	 public void setFreqTone(){
		 
		//get the selected note from the group of radio buttons
		 final RadioGroup rgNotes = (RadioGroup) findViewById(R.id.rgNotes);
		 
		 RadioButton selectRadio = (RadioButton) findViewById(rgNotes.getCheckedRadioButtonId());
		 note = selectRadio.getText().toString();
    	 
         //Set the frequency of the selected note
         freqOfTone=getFrequency(note.charAt(0));
  	 
	 }
	 
	 //Referesh the feedback bars and set all of them empty
	 public void refresh(){
		 RatingBar feedbackBar = (RatingBar) findViewById(R.id.feedbackBar);
		 ProgressBar lowBar= (ProgressBar) findViewById(R.id.lowBar);
		 ProgressBar highBar= (ProgressBar) findViewById(R.id.highBar);
		 
		 feedbackBar.setRating(0);
		 lowBar.setProgress(0);
		 highBar.setProgress(0);
		 
	 }
	 
	 //Start recording voice
	 public void startRecording(){
		 
		 	
		 //get the buffer size to use with this audio record
		 bufferSize = AudioRecord.getMinBufferSize(samplerate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)*3;
		 bufferSize =(int)Math.pow(2, Integer.toBinaryString(bufferSize).length());
		
		 //instantiate the AudioRecorder
		 recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,samplerate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,bufferSize); 
		 	
		 isRecording = true; //variable to use start or stop recording
		 audioData = new short [bufferSize]; //short array that pcm data is put into.

	  	// Set the end time to startTime + 2s 
		 long startTime = System.currentTimeMillis();
		 final long endTime = startTime + 2*1000; 
		 recorder.startRecording();
		 
		 final Thread recordingThread = new Thread(new Runnable() {
	            
			public void run() {
				while(System.currentTimeMillis() < endTime){
							recorder.read(audioData,0,bufferSize);
	       		}
							isRecording=false;
							enableButtons_Rec(false);
							stopRecording();
							analyze();
						    
	                         }
	            }
	        );
	        recordingThread.run();
	    }
	 
	 //Stop recording voice
	 public void stopRecording(){
		 if (null != recorder) {
		        isRecording = false;
		        Toast.makeText(Selection.this, "Done " ,Toast.LENGTH_LONG).show();
		    	
		        recorder.stop();
		        recorder.release();

		        recorder = null;
		        recordingThread = null;
		       
		 }
		 
	 }
	  
	 //Set handlers for buttons
	 private void setButtonHandlers() {
		    ((Button) findViewById(R.id.btnRecord)).setOnClickListener(btnClick);
		    ((Button) findViewById(R.id.btnPlay)).setOnClickListener(btnClick);
			
	 }
	 
	 //Set onclick listners of buttons
	 private View.OnClickListener btnClick = new View.OnClickListener() {
		    public void onClick(View v) {
		        switch (v.getId()) {
		        case R.id.btnRecord: {
		        	enableButtons_Rec(true);
		            refresh();
		            setFreqTone();
		            startRecording();
		            break;
		        }
		        case R.id.btnPlay: {
		            enableButtons_Play(true);
		            play();
		            break;
		        }
		        
		        }
		    }
		};
		
	//Enable the selected button
	private void enableButton(int id, boolean isEnable) {
		    ((Button) findViewById(id)).setEnabled(isEnable);
		}
	 //Select buttons which need to be enabled when recording voice
	private void enableButtons_Rec(boolean isRecording) {
		    enableButton(R.id.btnRecord, !isRecording);
		    enableButton(R.id.btnPlay, !isRecording);
		}
	
	//Select buttons which need to be enabled when playing a note
	private void enableButtons_Play(boolean isPlaying) {
		    enableButton(R.id.btnRecord, !isPlaying);
		    enableButton(R.id.btnPlay, !isPlaying);
		}

		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);
		setButtonHandlers();
		

}
	//play the selected note
	public void play(){
	  isPlaying=true;
		
	  setFreqTone(); 
        
      final Thread thread = new Thread(new Runnable() {
      public void run() {
          genTone();
          handler.post(new Runnable() {

              public void run() {
                  playSound();
                 
                    	try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
      	        isPlaying =false;
                enableButtons_Play(isPlaying);
                	
                  
              }
          });
      }
  });
    thread.start();
  	
	}
	//Get frequency of a note
	public double getFrequency(char note){
		 switch(note){
      	case 'C':
      		return(261.63);
      	case 'D':
      		return(293.66);
      	case 'E':
      		return(329.63);
      	case 'F':
      		return(349.23);
      	case 'G':
      		return(392.00);
      	case 'A':
      		return(440);
      	case 'B':
      		return(493.88);
      	default:
      		return (261.63);
	 }

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

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(isRecording){
			stopRecording();
			enableButtons_Rec(isRecording);
			
		}
	}
	
	
}
