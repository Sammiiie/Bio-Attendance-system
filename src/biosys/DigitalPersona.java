/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package biosys;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.digitalpersona.onetouch.*;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.DPFPCapturePriority;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.capture.event.DPFPDataListener;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusEvent;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.digitalpersona.onetouch.readers.DPFPReaderDescription;
import com.digitalpersona.onetouch.readers.DPFPReadersCollection;
import com.digitalpersona.onetouch.ui.swing.*;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.digitalpersona.onetouch.verification.DPFPVerificationResult;
import javax.swing.JOptionPane;
/**
 *
 * @author Ejiga Samuel
 */
public class DigitalPersona {
    static byte[] temp;
   
    Connection con = null;
    PreparedStatement pst = null;
    ResultSet result = null;
    
    static EnumMap<DPFPFingerIndex, DPFPTemplate> templates = new EnumMap<DPFPFingerIndex, DPFPTemplate>(DPFPFingerIndex.class);
    
    public void insert(byte[] digital, String rel){
                            
		try { 
                        con = Connector.connectDB();
                        String sql = "INSERT INTO biometric (bio, idstaff) VALUES(?,?)";
                        pst = con.prepareStatement(sql);
                        
                        
                        
			//pst.setInt(1, id);
                        pst.setBytes(1, digital);
                        pst.setString(2, rel);
			pst.executeUpdate();
		} 
                catch (SQLException e) {  
                    System.out.println(e.getMessage());
                    JOptionPane.showMessageDialog(null, e);
                } 
	}
    
    public byte[] get(){ 
		
		byte[] digital = null;
		try { 
			String sel = "SELECT * FROM biometric";
                        pst = con.prepareStatement(sel);
			result = pst.executeQuery();
			if(result.next())
				digital = result.getBytes("bio");
			else 
                            JOptionPane.showMessageDialog(null, "Record not available");
			 
		} catch (SQLException e) {
			System.out.println(e.getMessage());
                        JOptionPane.showMessageDialog(null, e);
		} 
		 
		return digital;
	}
    
    public static void listReaders() { 
        DPFPReadersCollection readers = DPFPGlobal.getReadersFactory().getReaders();
        if (readers == null || readers.size() == 0) {
            JOptionPane.showMessageDialog(null, "There are no readers available.");
            
            return; 
        }
        
        for (DPFPReaderDescription readerDescription : readers){
            //System.out.println(readerDescription.getSerialNumber());
            JOptionPane.showMessageDialog(null, "Available readers: %s", readerDescription.getSerialNumber(), 0);
            
        }
    }
    
    //Digital Persona's classes for fingers
	public static final EnumMap<DPFPFingerIndex, String> fingerNames;
    static { 
    	fingerNames = new EnumMap<DPFPFingerIndex, String>(DPFPFingerIndex.class);
    	fingerNames.put(DPFPFingerIndex.LEFT_PINKY,	  "left pinky");
    	fingerNames.put(DPFPFingerIndex.LEFT_RING,    "left ring");
    	fingerNames.put(DPFPFingerIndex.LEFT_MIDDLE,  "left middle");
    	fingerNames.put(DPFPFingerIndex.LEFT_INDEX,   "left index");
    	fingerNames.put(DPFPFingerIndex.LEFT_THUMB,   "left thumb");
    	fingerNames.put(DPFPFingerIndex.RIGHT_PINKY,  "right pinky");
    	fingerNames.put(DPFPFingerIndex.RIGHT_RING,   "right ring");
    	fingerNames.put(DPFPFingerIndex.RIGHT_MIDDLE, "right middle");
    	fingerNames.put(DPFPFingerIndex.RIGHT_INDEX,  "right index");
    	fingerNames.put(DPFPFingerIndex.RIGHT_THUMB,  "right thumb");
    }
    
    //Creating fingerprint data to store
    public DPFPTemplate getTemplate(String activeReader, int nFinger) {
        JOptionPane.showMessageDialog(null, "Performing fingerprint enrollment...");
         
        DPFPTemplate template = null;
         
        try { 
            DPFPFingerIndex finger = DPFPFingerIndex.LEFT_THUMB;
            DPFPFeatureExtraction featureExtractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
            DPFPEnrollment enrollment = DPFPGlobal.getEnrollmentFactory().createEnrollment();
             
            while (enrollment.getFeaturesNeeded() > 0)
            { ;
                DPFPSample sample = getSample(activeReader, 
                	String.format("Scan your %s finger (%d remaining)\n", fingerName(finger), enrollment.getFeaturesNeeded()));
                if (sample == null)
                    continue; 
 
 
                DPFPFeatureSet featureSet;
                try { 
                    featureSet = featureExtractor.createFeatureSet(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);
                } catch (DPFPImageQualityException e) {
                    //System.out.printf("Bad image quality: \"%s\". Try again. \n", e.getCaptureFeedback().toString());
                    continue; 
                } 
 
 
                enrollment.addFeatures(featureSet);
            } 
            template = enrollment.getTemplate();
            JOptionPane.showMessageDialog(null, "The %s was enrolled.\n", fingerName(finger), enrollment.getFeaturesNeeded());
            System.out.printf(fingerprintName(finger));
        } catch (DPFPImageQualityException e) {
            JOptionPane.showMessageDialog(null, "Failed to enroll the finger.");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } 
         
        return template;
        
    }
    
    public boolean verify(String activeReader, DPFPTemplate template) {
		 
		 
        try { 
            DPFPSample sample = getSample(activeReader, "Scan your finger\n");
            if (sample == null)
                throw new Exception();
 
 
            DPFPFeatureExtraction featureExtractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
            DPFPFeatureSet featureSet = featureExtractor.createFeatureSet(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);
			 
            DPFPVerification matcher = DPFPGlobal.getVerificationFactory().createVerification();
            matcher.setFARRequested(DPFPVerification.MEDIUM_SECURITY_FAR);
             
            for (DPFPFingerIndex finger : DPFPFingerIndex.values()) {
                //DPFPTemplate template = user.getTemplate(finger); 
                if (template != null) {
                    DPFPVerificationResult result = matcher.verify(featureSet, template);
                    if (result.isVerified()) {
                        JOptionPane.showMessageDialog(null, "Matching finger: %s, FAR achieved: %g.");
                        System.out.printf(fingerName(finger), (double)result.getFalseAcceptRate()/DPFPVerification.PROBABILITY_ONE);
                        return result.isVerified();
                    } 
                } 
            } 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to perform verification.");
        } 
         
        return false; 
    }
    
    public DPFPSample getSample(String activeReader, String prompt)
	throws InterruptedException
	{ 
	    final LinkedBlockingQueue<DPFPSample> samples = new LinkedBlockingQueue<DPFPSample>();
	    DPFPCapture capture = DPFPGlobal.getCaptureFactory().createCapture();
	    capture.setReaderSerialNumber(activeReader);
	    capture.setPriority(DPFPCapturePriority.CAPTURE_PRIORITY_LOW);
	    capture.addDataListener(new DPFPDataListener()
	    { 
	        public void dataAcquired(DPFPDataEvent e) {
	            if (e != null && e.getSample() != null) {
	                try { 
	                    samples.put(e.getSample());
	                } catch (InterruptedException e1) {
	                    e1.printStackTrace();
	                } 
	            } 
	        } 
	    }); 
	    capture.addReaderStatusListener(new DPFPReaderStatusAdapter()
	    { 
	    	int lastStatus = DPFPReaderStatusEvent.READER_CONNECTED;
			public void readerConnected(DPFPReaderStatusEvent e) {
				if (lastStatus != e.getReaderStatus())
                                        JOptionPane.showMessageDialog(null, "Reader is connected");
				lastStatus = e.getReaderStatus();
			} 
			public void readerDisconnected(DPFPReaderStatusEvent e) {
				if (lastStatus != e.getReaderStatus())
                                    JOptionPane.showMessageDialog(null, "Reader is disconnected");
				lastStatus = e.getReaderStatus();
			} 
	    	 
	    }); 
	    try { 
	        capture.startCapture();
	        System.out.print(prompt);
	        return samples.take();
	    } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(null, "Failed to start capture. Check that raeder is not used by another application.");
	        throw e;
	    } finally { 
	        capture.stopCapture();
	    } 
	}
    
    public String fingerName(DPFPFingerIndex finger) {
    	return fingerNames.get(finger); 
    } 
    public String fingerprintName(DPFPFingerIndex finger) {
    	return fingerNames.get(finger) + " fingerprint"; 
    }
    
}
