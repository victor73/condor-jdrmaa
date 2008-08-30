package net.sf.igs;

/*
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ggf.drmaa.AlreadyActiveSessionException;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.InternalException;
import org.ggf.drmaa.InvalidContactStringException;
import org.ggf.drmaa.InvalidJobException;
import org.ggf.drmaa.InvalidJobTemplateException;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.NoActiveSessionException;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.Version;

/**
 * The SessionImpl class provides a DRMAA interface to Condor.
 *
 * @see org.ggf.drmaa.Session
 * @see net.sf.igs.JobTemplateImpl
 * @see "http://www.cs.wisc.edu/condor/"
 */
public class SessionImpl implements Session {
    private static final String SUBMIT_FILE_PREFIX = "condor_drmaa_";
	private static final String DRM_SYSTEM = "Condor";
	private String contact = "";
    private boolean activeSession = false;
    private File sessionDir = null;
    private int jobTemplateId = 1;
    
    // Sleep period is in seconds
    private static final int SLEEP_PERIOD = 5;
    
    /**
     * Creates a new instance of SessionImpl
     */
    public SessionImpl() {
    	// No-arguments constructor
    }
    
    /**
     * <p>Controls Condor jobs.</p>
     *
     * {@inheritDoc}
     *
     * TODO: Complete
     * <p>The DRMAA suspend/resume operations are equivalent to the use of</p>
     *
     * TODO: Complete
     * <p>The DRMAA hold/release operations are equivalent to the use of</p>
     *
     * TODO: Complete
     * <p>The DRMAA terminate operation is equivalent to the use of</p>
     *
     * <p>Only user hold and user suspend can be controlled via control().  For
     * affecting system hold and system suspend states the appropriate DRM
     * interfaces must be used.</p>
     *
     * @param jobId {@inheritDoc}
     * @param action {@inheritDoc}
     * @throws DrmaaException {@inheritDoc}
     */
    public void control(String jobId, int action) throws DrmaaException {
      	// this.nativeControl(jobId, action);
    	// TODO: Implement
    }
    
    /**
     * The exit() method closes the DRMAA session for all threads and must be
     * called before process termination.  The exit() method may be called only
     * once by a single thread in the process and may only be called after the
     * init() function has completed.  Any call to exit() before init() returns
     * or after exit() has already been called will result in a
     * NoActiveSessionException.
     *
     * <p>The exit() method does necessary clean up of the DRMAA session state</p>
     *
     * <p>Submitted jobs are not affected by the exit() method.</p>
     *
     * @throws DrmaaException {@inheritDoc}
     */
    public void exit() throws DrmaaException {
    	if (activeSession) {
        	if (sessionDir != null && sessionDir.exists()) {
        		Util.deleteDir(sessionDir);
        	}
            activeSession = false;
    	} else {
    		throw new IllegalStateException();
    	}
    }

    /**
     * getContact() returns an opaque string containing contact information
     * related to the current DRMAA session to be used with the {@link #init(String) init}
     * method.
     *
     * <p>Before the init() method has been called, this method will always
     * return an empty string.
     * 
     * @return {@inheritDoc}
     * @see #init(String)
     */
    public String getContact() {
    	return contact;
    }

    /**
     * The getDRMSystem() method returns a string containing the DRM information.
     *
     * @return {@inheritDoc}
     */
    public String getDrmSystem() {
        return DRM_SYSTEM;
    }
    
    /**
     * The getDrmaaImplementation() method returns a string containing the DRMAA
     * Java language binding implementation version information.  The
     * method returns the same value before and after {@link #init(String) init} is called.
     * 
     * @return {@inheritDoc}
     */
    public String getDrmaaImplementation() {
        /* Because the DRMAA implementation is tightly bound to the DRM, there's
         * no need to distinguish between them.  Version information can be
         * gotten from getVersion() and language information is self-evident. */
        return this.getDrmSystem();
    }
    
    /**
     * <p>Get the job program status.</p>
     *
     * {@inheritDoc}
     *
     * <p>The control method can be used to control job submitted outside of the scope
     * of the DRMAA session as long as the job identifier for the job is known.</p>
     * @return {@inheritDoc}
     * @param jobId {@inheritDoc}
     * @throws DrmaaException {@inheritDoc}
     */
    public int getJobProgramStatus(String jobId) throws DrmaaException {
        // TODO: Implement
    	return 0;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @return {@inheritDoc}
     * @throws DrmaaException {@inheritDoc}
     */
    public JobTemplate createJobTemplate() throws DrmaaException {
        File jtFile = getJobTemplateFile(jobTemplateId);
        try {
			jtFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			throw new InternalException("Unable to create job template: " + e.getMessage());
		}
        JobTemplate jt = new JobTemplateImpl(this, jobTemplateId);
        synchronized (jt) {
        	jobTemplateId++;
		}
        return jt;
    }
    
    private File getJobTemplateFile(int templateId) {
        File jtFile = new File(sessionDir, jobTemplateId + "");
        return jtFile;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @param jt {@inheritDoc}
     * @throws DrmaaException {@inheritDoc}
     */
    public void deleteJobTemplate(JobTemplate jt) throws DrmaaException {
        if (jt == null) {
            throw new NullPointerException("JobTemplate is null");
        } else if (jt instanceof JobTemplateImpl) {
        	JobTemplateImpl job = (JobTemplateImpl) jt;
        	int id = job.getId();

        	// Delete the file from the session area
        	File jobSessionFile = getJobTemplateFile(id);
        	if (jobSessionFile.exists() && jobSessionFile.isFile()) {
        		jobSessionFile.delete();
        	}
        	job = null;
        	jt = null;
        } else {
            throw new InvalidJobTemplateException();
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * @return {@inheritDoc}
     */
    public Version getVersion() {
        return new Version(0, 2);
    }
    
    /**
     * The init() method initializes the Condor DRMAA API library for
     * all threads of the process and creates a new DRMAA Session. This routine
     * must be called once before any other DRMAA call, except for
     * getDrmSystem(), getContact(), and getDrmaaImplementation().
     *
     * <p><i>contact</i> is an implementation dependent string.  The contact
     * string is composed of a series of name=value pairs separated by semicolons.
     * The supported name=value pairs are:</p>
     *
     * <ul>
     *    <li>
     *      <code>session</code>: the id of the session to which to reconnect
     *    </li>
     * </ul>
     *
     * <p>The <i>contact</i> may be null or empty.
     *
     * <p>Except for the above listed methods, no DRMAA methods may be called
     * before the init() function <b>completes</b>.  Any DRMAA method which is
     * called before the init() method completes will throw a
     * NoActiveSessionException.  Any additional call to init() by any thread
     * will throw a SessionAlreadyActiveException.</p>
     *
     * <p>Once init() has been called, it is the responsibility of the developer
     * to ensure that the exit() will be called before the program
     * terminates.</p>
     *
     * @param contact {@inheritDoc}
     * @throws DrmaaException {@inheritDoc}
     * @see #getContact()
     * @see #exit()
     */
    public void init(String contact) throws DrmaaException {
    	if (contact == null || contact.length() == 0) {
    		throw new InvalidContactStringException();
    	}
    	
    	boolean condorPresent = Util.isCondorAvailable();
    	if (! condorPresent) {
    		throw new InternalException("No Condor installation found.");
    	}

        // Make the directory for the session
        String topDir = Util.TMP + File.separator + "condor-jdrmaa-" + System.getProperty("user.name");

    	synchronized (contact) {
        	if (activeSession) {
        		throw new AlreadyActiveSessionException();
        	}
            this.contact = contact;

            sessionDir = new File(topDir, contact);
            if (sessionDir.exists()) {
            	boolean deleted = Util.deleteDir(sessionDir);
            	if (! deleted) {
            		throw new InternalException("Unable to delete " + sessionDir + " and it is in the way.");
            	}
            }
            sessionDir.mkdirs();
            sessionDir.deleteOnExit();
            activeSession = true;
		}
    }

    /**
     * The runBulkJobs() method submits an array job very much as
     * if the condor_q options for array jobs had been used
     * with the corresponding attributes defined in the DRMAA JobTemplate,
     * <i>jt</i>.
     *
     * <p>On success a String array containing job identifiers for each array
     * job task is returned.</p>
     *
     * @return {@inheritDoc}
     * @param start {@inheritDoc}
     * @param end {@inheritDoc}
     * @param incr {@inheritDoc}
     * @param jt {@inheritDoc}
     * @throws DrmaaException {@inheritDoc}
     */
    public List<String> runBulkJobs(JobTemplate jt, int start, int end, int incr) throws DrmaaException {
        if (! activeSession) {
        	throw new NoActiveSessionException(); 
        }
    	if (jt == null) {
            throw new NullPointerException("JobTemplate is null");
        } else if (! (jt instanceof JobTemplateImpl)) {
            throw new InvalidJobTemplateException();
        }

        if (incr != 1) {
			throw new InvalidJobTemplateException(
					"This version of Condor-JDRMAA only supports increments of 1");
		}

		if (start > end) {
			throw new InvalidJobTemplateException(
					"This version of Condor-JDRMAA does not support decreasing ranges.");
		}

		try {
			int number = end - start + 1;
			File submitFile = createSubmitFile(jt, number);
			// The submit() method returns the first job id regardless of whether the job
			// is a singleton or an array job. Therefore, we should always get a job ID with
			// a trailing ".0" on it. In the runBulkJobs context, we remove this and recalculate
			// the array suffixes sequentially. For this reason, we can't really support
			// increments other than 1, since Condor doesn't seem to have an easy way to do it
			// in the submit file either...
			String jobId = submit(submitFile);
			jobId = jobId.replace(".0", "");
			
			ArrayList<String> jobs = new ArrayList<String>();
			for (int jobIndex = 0; jobIndex < end; jobIndex++) {
				String fullJobId = jobId + "." + jobIndex;
				jobs.add(fullJobId);
			}
			return jobs;
		} catch (Exception e) {
			throw new InvalidJobTemplateException(e.getMessage());
		}
    }

    /**
     * The runJob() method submits a Condor job with attributes defined in
     * the DRMAA JobTemplate <i>jt</i>. On success, the job identifier is
     * returned.
     * @param jt {@inheritDoc}
     * @throws DrmaaException {@inheritDoc}
     * @return {@inheritDoc}
     */
    public String runJob(JobTemplate jt) throws DrmaaException {
    	String jobId = null;
        if (jt == null) {
            throw new NullPointerException("JobTemplate is null");
        } else if (! (jt instanceof JobTemplateImpl)) {
            throw new InvalidJobTemplateException();
        }
        try {
        	File submitFile = createSubmitFile(jt, 1);
			jobId = submit(submitFile);
			// Save the retrieved jobId in the session file
			saveJobIdInSessionFile(jt, jobId);
        } catch (Exception e) {
        	throw new InternalException(e.getMessage());
        }
        return jobId;
    }

	/**
	 * @param jt
	 * @param jobId
	 * @throws IOException
	 */
	private void saveJobIdInSessionFile(JobTemplate jt, String jobId)
			throws IOException {
		File templateFile = getJobTemplateFile(((JobTemplateImpl) jt).getId());
		BufferedWriter w = new BufferedWriter(new FileWriter(templateFile));
		w.write(jobId);
		w.newLine();
		w.close();
	}
    
    /**
     * Create a Condor submit file for the job template.
     * 
     * @param job a {@link JobTemplate}
     * @param number the number of times to execute the job
     * @return a {@link File} for the submit file created
     * @throws Exception
     */
    private File createSubmitFile(JobTemplate job, int number) throws Exception {
    	if (number <= 0) {
    		throw new IllegalArgumentException("Job count must be a positive integer.");
    	}
    	
    	File tempFile = null;
    	
		try {
	    	tempFile = File.createTempFile(SUBMIT_FILE_PREFIX, null);
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
			writer.write("# Condor Submit file");
			writer.newLine();
			writer.write("# Generated by Condor-JDRMAA");
			writer.newLine();
			writer.write("#");
			writer.newLine();
			writer.write("Log=" + Util.LOG_TEMPLATE);
			writer.newLine();
			writer.write("Universe=vanilla");
			writer.newLine();
			writer.write("Executable=" + job.getRemoteCommand());
			writer.newLine();
			if (job.getJobSubmissionState() == JobTemplate.HOLD_STATE) {
				writer.write("Hold=true");
				writer.newLine();
			}
			
			// Here we handle the job arguments, if any have been supplied.
			// We try to adhere to the "new" way of specifying the arguments
			// as explained in the 'condor_submit' man page.
			if (job.getArgs() != null && job.getArgs().size() > 0) {
				StringBuffer sb = new StringBuffer();
				sb.append("\"");
				char tick = '\'';
				Iterator<String> iter = job.getArgs().iterator();
				while (iter.hasNext()) {
					String arg = iter.next();
					if (arg.contains("\"")) {
						arg = arg.replace("\"", "\"\"");
					}
					// Replace ticks with double ticks
					if (arg.contains("\'")) {
						arg = arg.replace("\'", "\'\'");
					}
					if (arg.contains(" ")) {
						sb.append(tick).append(arg).append(tick);
					} else {
						sb.append(arg);
					}
					if (iter.hasNext()) {
						sb.append(" ");
					}
				}
				sb.append("\"");
				writer.write("Arguments=" + sb.toString());
				writer.newLine();
			}
			
			// If the working directory has been set, configure it.
			if (job.getWorkingDirectory() != null) {
				writer.write("InitialDir = " + job.getWorkingDirectory());
				writer.newLine();
			}
			
			// Handle any native specifications that have been set
			if (job.getNativeSpecification() != null) {
				writer.write(job.getNativeSpecification());
				writer.newLine();
			}
			
			// Handle the job category. This is handled the same way as the
			// native specification.
			if (job.getJobCategory() != null) {
				writer.write(job.getJobCategory());
				writer.newLine();
			}
			
			// Send email notifications?
			if (job.getBlockEmail()) {
				writer.write("Notification=Never");
			}
			
			if (job.getStartTime() != null) {
				long time = job.getStartTime().getTimeInMillis() / 1000;
				writer.write("PeriodicRelease=(CurrentTime > " + time + ")");
				writer.newLine();
				writer.write("Hold=True");
				writer.newLine();
			}
			
			if (job.getJobName() != null) {
				writer.write("+JobName=" + job.getJobName());
				writer.newLine();
			}
			
			if (job.getInputPath() != null) {
				String input = job.getInputPath();
				input = input.replace(JobTemplate.PARAMETRIC_INDEX, "$(Process)");
				input = input.replace(JobTemplate.HOME_DIRECTORY,  "$ENV(HOME)");
				if (input.startsWith(":")) {
					input = input.substring(1);
				}
				writer.write("Input=" + input);
				writer.newLine();
				// Check whether to transfer the input files
				if (job.getTransferFiles().getInputStream()) {
					writer.write("transfer_input_files=i");
					writer.newLine();
				}
			}
			
			if (job.getOutputPath() != null) {
				String output = job.getOutputPath();
				output = output.replace(JobTemplate.PARAMETRIC_INDEX, "$(Process)");
				output = output.replace(JobTemplate.HOME_DIRECTORY, "$ENV(HOME)");
				if (output.startsWith(":")) {
					output = output.substring(1);
				}
				writer.write("Output=" + output);
				writer.newLine();
				
				// Check if we need to join input and output files
				if (job.getJoinFiles()) {
					writer.write("# Joining Input and Output");
					writer.newLine();
					writer.write("Error=" + output);
					writer.newLine();
				}
			}
			
			// Handle the error path if specified. Do token replacement if necessary.
			if (job.getErrorPath() != null && ! job.getJoinFiles()) {
				String error = job.getErrorPath();
				error = error.replace(JobTemplate.PARAMETRIC_INDEX, "$(Process)");
				error = error.replace(JobTemplate.HOME_DIRECTORY, "$ENV(HOME)");
				if (error.startsWith(":")) {
					error = error.substring(1);
				}
				writer.write("Error=" + error);
				writer.newLine();
			}
			
			if (job.getTransferFiles() != null && job.getTransferFiles().getOutputStream()) {
				writer.write("should_transfer_files=IF_NEEDED");
				writer.newLine();
				writer.write("when_to_transfer_output=ON_EXIT");
				writer.newLine();
			}
			
			if (job.getJobEnvironment() != null && ! job.getJobEnvironment().isEmpty()) {
				Map<String, String> environment = job.getJobEnvironment();
				StringBuffer sb = new StringBuffer();
				Iterator<String> iter = environment.keySet().iterator();
				while (iter.hasNext()) {
					String name = (String) iter.next();
					String value = (String) environment.get(name);
					value = value.replace("\"", "\"\"");
					String pair = name + "=" + value;
					sb.append(pair);
					if (! iter.hasNext()) {
						sb.append(" ");
					}
				}
				writer.write("Environment=\"" + sb.toString() + "\"");
				writer.newLine();
			}
			
			// It appears that Condor can only handle 1 email address for notifications
			// while the DRMAA returns a set of them. If we have emails specified, then
			// just use the first one...
			if (job.getEmail() != null && job.getEmail().size() > 0) {
				if (job.getEmail().size() > 1) {
					System.err.println("Warning: Only 1 email address is supported.");
				}
				writer.write("Notify_user=" + (String) job.getEmail().iterator().next());
				writer.newLine();
			}
			
			writer.write("Queue " + number);
			writer.newLine();
			
			// Close the writer
			writer.close();
		} catch (IOException e) {
			throw new Exception("Unable to create the Condor submit file.");
		}
		return tempFile;
    }

    /*
     * Invokes 'condor_submit' to submit the job to the Condor scheduler. The submit
     * file is deleted after submission is complete. To leave the submit file in place,
     * for debugging perhaps, define the condor.jdrmaa.debug system property.
     */
    private String submit(File submitFile) throws Exception {
    	if (! (submitFile.exists() && submitFile.isFile() && submitFile.canRead())) {
    		throw new IllegalArgumentException("Submit file does not exist or isn't readable.");
    	}
    	
    	String jobID = null;
    	
    	try {
    		String submitPath = submitFile.getAbsolutePath();
        	String[] command = {"condor_submit", submitPath};
        	Process process = Runtime.getRuntime().exec(command);
			process.waitFor();
	    	Reader reader = new InputStreamReader(process.getInputStream());
	    	BufferedReader bufReader = new BufferedReader(reader);
	    	String line = null;
	    	while ( (line = bufReader.readLine()) != null ) {
	    		if (line.contains("submitted to cluster")) {
	    			Pattern pattern = Pattern.compile("\\d+\\.$");
	    			Matcher matcher = pattern.matcher(line);
	    			if (matcher.find()) {
	    				jobID = matcher.group();
	    				jobID = jobID + "0";
	    			}
	    		}
	    	}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (System.getProperty("condor.jdrmaa.debug") == null) {
				submitFile.delete();
			}
		}


		return jobID;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @param jobIds {@inheritDoc}
     * @param timeout {@inheritDoc}
     * @param dispose {@inheritDoc}
     * @throws DrmaaException {@inheritDoc}
     */
    public void synchronize(List jobIds, long timeout, boolean dispose) throws DrmaaException {
    	// TODO: Implement
    	// this.nativeSynchronize((String[]) jobIds.toArray(new String[jobIds.size()]), timeout, dispose);
    	// TODO: Handle case of JOB_IDS_SESSION_ALL
    }
    
    /**
     * {@inheritDoc}
     * 
     * @param jobId {@inheritDoc}
     * @param timeout {@inheritDoc}
     * @throws DrmaaException {@inheritDoc}
     */
    public JobInfo wait(String jobId, long timeout) throws DrmaaException {
    	// Make sure we have a good timeout, but check that it's not one of the
    	// predefined values.
    	if (timeout <= 0 && timeout != Session.TIMEOUT_WAIT_FOREVER && timeout != Session.TIMEOUT_NO_WAIT) {
    		throw new IllegalArgumentException("Must have a positive timeout.");
    	}
    	

        // Check if we have a valid job ID
        if (! (Util.validJobId(jobId) || jobId.equals(Session.JOB_IDS_SESSION_ANY))) {
        	throw new InvalidJobException();
        }
        
        // Check if we have a bad timeout value
        if (timeout < 0 && timeout != SessionImpl.TIMEOUT_WAIT_FOREVER) {
        	throw new IllegalArgumentException("Illegal timeout value.");
        }
        
        if (jobId.equals(SessionImpl.JOB_IDS_SESSION_ANY)) {
        	File[] files = sessionDir.listFiles();
        	if (files.length > 0) {
        		// TODO: Pick a file at random instead of just the first
        		File toWaitFor = files[0];
        		try {
					BufferedReader buf = new BufferedReader(new FileReader(toWaitFor));
					jobId = buf.readLine().trim();
				} catch (Exception e) {
					throw new InternalException("Unable to read file " + toWaitFor.getAbsolutePath());
				}
        	}
        }
        
        JobInfo info = null;
    	try {
            JobLogParser logParser = new JobLogParser(jobId);
			info = logParser.parse();
	    	// Only go into a monitoring loop if we have a bonafide timeout
	        if (timeout != Session.TIMEOUT_NO_WAIT) {
	            info = monitor(logParser, timeout);
	        }
		} catch (Exception ioe) {
			ioe.printStackTrace();
			throw new InternalException(ioe.getMessage());
		}
    	
        return info;
    }

	private JobInfo monitor(JobLogParser logParser, long timeout) throws Exception {
    	// Get the current number of seconds since the epoch. We'll refer
    	// to this to make sure we stop waiting if we reach the timeout...
    	long start = System.currentTimeMillis() / 1000;
        
		boolean done = false;
		JobInfo info = null;
		// Start monitoring
		while (! done) {
		    info = logParser.parse();
		    if (info.hasExited()) {
		    	done = true;
		    }
		    if (! done && timeout == Session.TIMEOUT_WAIT_FOREVER) {
		        long now = System.currentTimeMillis() / 1000;
		        if ((now - start) >= timeout) {
		        	// We've reached the timeout
		        	done = true;
		        }
		    }
		    
		    if (! done) {
		    	try {
		    		// SLEEP_PERIOD is in seconds
					Thread.sleep(SLEEP_PERIOD * 1000);
				} catch (InterruptedException e) {
					break;
				}
		    }
		}
		return info;
	}
}