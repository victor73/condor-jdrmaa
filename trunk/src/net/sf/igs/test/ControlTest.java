package net.sf.igs.test;

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.igs.CondorExecException;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.NoActiveSessionException;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests whether support for the {@link Session#control(String, int) control()}
 * method works properly. The tests contained in this class work by submitting
 * sleep jobs and then manipulating them by suspending them, terminating them,
 * holding and then releasing them, etc. At the conclusion of the tests, an
 * effort is made to cleanup after ourselves by removing any leftover test jobs
 * from the grid.
 * 
 * @see "The DRMAA 1.0 Java specification: ggf-drmaa-java-binding.1_0.pdf"
 */
public class ControlTest {
	private static String name = ControlTest.class.getSimpleName();
	private static Set<String> jobIdsToCleanup;
	
	// Okay, this regular expression is rather ugly, but it is used to extract
	// the status of a job on the grid when processing a line output from the
	// 'condor_q' utility from Condor. The groups (in parentheses) are as follows:
	// 1 - The job ID
	// 2 - The job owner's username
	// 3 - The date submitted
	// 4 - The time submitted
	// 5 - The run time
	// 6 - The status code
	private static Pattern condorQueueRegex =
		Pattern.compile("^(\\d+\\.\\d+)\\s+(\\w+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s(\\w)");
	
	/**
	 * Initialize the test class. During the initialization, a data structure
	 * is created to hold job IDs created by the testing. When the testing is
	 * concluded, each of the job IDs is reaped by the cleanup process.
	 */
	@BeforeClass
	public static void setup() {
		jobIdsToCleanup = new HashSet<String>();
	}
	
	/**
	 * Tests whether a {@link NoActiveSessionException} exception is thrown
	 * when we attempt to control a job outside of a session.
	 * 
	 * @throws DrmaaException
	 */
	@Test(expected=NoActiveSessionException.class)
	public void testControlWithoutSession() throws DrmaaException {
		Session session = SessionFactory.getFactory().getSession();
		JobTemplate sleepTemplate = getSleepJobTemplate(session);
		
		// Run the job and get the ID.
		String jobId = session.runJob(sleepTemplate);
		session.deleteJobTemplate(sleepTemplate);
		session.exit();
		
		// Now that we have exited the session, try to use the session
		// to control the job we have previously submitted. This should
		// throw an exception.
		session.control(jobId, Session.TERMINATE);
		
		// If we get here, then the test failed, which means a job actually
		// went through. Mark it for removal during the cleanup process.
		jobIdsToCleanup.add(jobId);
		fail("No exception thrown");
	}
	
	
	/**
	 * Test the {@link Session#control(String, int)} method for terminating a job.
	 */
	@Test
	public void testJobTerminate() {
		try {
			Session session = SessionFactory.getFactory().getSession();
			session.init(name);
			
			JobTemplate jt = getSleepJobTemplate(session);
			
			String jobId = session.runJob(jt);
			assertNotNull(jobId);
			assertTrue(jobId.length() > 0);
			
			// Free job template resources
			session.deleteJobTemplate(jt);
			
			// Sleep a little...
			Thread.sleep(2000);
			
			// Make sure the job is on the grid.
			boolean present = isJobPresent(jobId);
			assertTrue(present);
			
			// Try suspending the job
			session.control(jobId, Session.TERMINATE);
			
			// Sleep a little more...
			Thread.sleep(2000);
			
			// Exit the session
			session.exit();
			
			// Determine if the job is present on the grid. We use
			// a private method to help us with this.
			present = isJobPresent(jobId);

			// The job should not be around (we killed it).
			assertFalse(present);
			
			if (present) {
				// Save the job ID for later removal. Can't have an accumulation
				// of held jobs just because we're testing can we?
				jobIdsToCleanup.add(jobId);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test the {@link Session#control(String, int)} method for suspending a job.
	 */
	@Test
	public void testJobSuspend() {
		try {
			Session session = SessionFactory.getFactory().getSession();
			session.init(name);
			
			JobTemplate jt = getSleepJobTemplate(session);
			
			String jobId = session.runJob(jt);
			assertNotNull(jobId);
			assertTrue(jobId.length() > 0);
			
			// Free job template resources
			session.deleteJobTemplate(jt);
			
			// Sleep a little...
			Thread.sleep(3000);
			
			// Make sure the job is on the grid.
			boolean present = isJobPresent(jobId);
			assertTrue(present);
			
			// Try suspending the job
			session.control(jobId, Session.SUSPEND);
			
			// Exit the session
			session.exit();
			
			boolean held = isJobHeld(jobId);
			if (held) {
				// Save the job ID for later removal. Can't have an accumulation
				// of held jobs just because we're testing can we?
				jobIdsToCleanup.add(jobId);
			}
			assertTrue(held);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test the {@link Session#control(String, int)} method for suspending a job.
	 */
	@Test
	public void testJobHold() {
		try {
			Session session = SessionFactory.getFactory().getSession();
			session.init(name);
			
			JobTemplate jt = getSleepJobTemplate(session);

			String jobId = session.runJob(jt);
			assertNotNull(jobId);
			assertTrue(jobId.length() > 0);
			
			// Free job template resources
			session.deleteJobTemplate(jt);
			
			// Sleep a little...
			Thread.sleep(3000);
			
			// Make sure the job is on the grid.
			boolean present = isJobPresent(jobId);
			assertTrue(present);
			
			// Try putting the job on hold
			session.control(jobId, Session.HOLD);
			
			// Exit the session
			session.exit();
			
			boolean held = isJobHeld(jobId);
			if (held) {
				// Save the job ID for later removal. Can't have an accumulation
				// of held jobs just because we're testing can we?
				jobIdsToCleanup.add(jobId);
			}
			
			assertTrue(held);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test the {@link Session#control(String, int)} method for resuming a held job.
	 */
	@Test
	public void testJobResume() {
		try {
			Session session = SessionFactory.getFactory().getSession();
			session.init(name);
			
			// Create a job that is suspended/held. This will let us test
			// the release/resume functionality.
			String jobId = createHeldJob(session);
			assertNotNull(jobId);
			
			// Add this job to the jobs to cleanup after tests are completed
			jobIdsToCleanup.add(jobId);
			
			// Release the job
			session.control(jobId, Session.RESUME);
			
			// Sleep a little, then make sure the job is not held anymore
			Thread.sleep(2000);
			boolean held = isJobHeld(jobId);
			assertFalse(held);
			
			// Might take a little while for the job to return to a running state.
			boolean running = false;
			for (int count = 1; count <= 10; count++) {
				// Verify that the job is now running
				running = isJobRunning(jobId);
				if (running) {
					break;
				} else {
					// Wait a little bit
					Thread.sleep(2000);
				}
			}
			assertTrue(running);
			
			// Exit the session
			session.exit();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test the {@link Session#control(String, int)} method for releasing a held job.
	 */
	@Test
	public void testJobRelease() {
		try {
			Session session = SessionFactory.getFactory().getSession();
			session.init(name);
			
			// Create a job that is suspended/held. This will let us test
			// the release/resume functionality.
			String jobId = createHeldJob(session);
			assertNotNull(jobId);
			
			// Add this job to the jobs to cleanup after tests are completed
			jobIdsToCleanup.add(jobId);
			
			// Release the job
			session.control(jobId, Session.RELEASE);
			
			// Sleep a little, then make sure the job is not held anymore
			Thread.sleep(2000);
			boolean held = isJobHeld(jobId);
			assertFalse(held);
			
			// Might take a little while for the job to return to a running state.
			boolean running = false;
			for (int count = 1; count <= 10; count++) {
				// Verify that the job is now running
				running = isJobRunning(jobId);
				if (running) {
					break;
				} else {
					// Wait a little bit
					Thread.sleep(2000);
				}
			}
			assertTrue(running);
			
			// Exit the session
			session.exit();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * The DMRAA 1.0 specification specifies that jobs submitted from other
	 * session CAN be controllable with this method. This Condor-JDRMAA
	 * implementation allows this. This method tests the feature by creating a
	 * session, submitting a job through it, deleting the template and closing
	 * the session. The test then starts a brand new session, with a different
	 * contact name, and controls the previously submitted job. It should work.
	 * 
	 * @see "ggf-drmaa-java-binding.1_0.pdf Section 5.1.27"
	 */
	@Test
	public void testHoldOutOfSession() {
		try {
			Session session = SessionFactory.getFactory().getSession();
			session.init(name);
			
			// Get a job template for a job that just sleeps
			JobTemplate jt = getSleepJobTemplate(session);
			
			// Execute the job and retrieve the job ID
			String jobId = session.runJob(jt);
			assertNotNull(jobId);
			assertTrue(jobId.length() > 0);
			
			// Free job template resources
			session.deleteJobTemplate(jt);
			
			// Sleep a little...
			Thread.sleep(2000);
			
			// Make sure the job is on the grid.
			boolean present = isJobPresent(jobId);
			assertTrue(present);
			
			// Exit the first session
			session.exit();
			session = null;
			
			// Now start a new session, with a different name.
			Session session2 = SessionFactory.getFactory().getSession();
			session2.init(name + "2");
			session2.control(jobId, Session.HOLD);
			
			// Sleep a little...
			Thread.sleep(2000);
			
			// Now check if we were able to hold a job that from a
			// different session.
			boolean held = isJobHeld(jobId);
			assertTrue(held);
			
			// We have another job that we need to reap during cleanup
			// when the tests conclude...
			if (held) {
				jobIdsToCleanup.add(jobId);
			}
			
			// Exit the second session
			session2.exit();
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/*
	 * Start a job, suspend it, and return the job ID. This is useful for the 
	 * "release" related tests.
	 */
	private String createHeldJob(Session session) throws DrmaaException, CondorExecException {
		try {
			JobTemplate jt = getSleepJobTemplate(session);

			String jobId = session.runJob(jt);
			
			// Free job template resources
			session.deleteJobTemplate(jt);
			
			// Sleep a little...
			Thread.sleep(2000);
			
			// Try putting the job on hold
			session.control(jobId, Session.HOLD);
			
			// TODO: Maybe have a loop here and check several times if the
			// first attempt shows the job isn't held yet...
			// Sleep a little more...
			Thread.sleep(2000);
			
			boolean held = isJobHeld(jobId);
			if (! held) {
				throw new CondorExecException("Unable to create a held job."); 
			}
			
			return jobId;
		} catch (InterruptedException ie) {
			throw new CondorExecException("Interrupted.", ie);
		}
	}
	
	/*
	 * Determine if a job is running.
	 */
	private boolean isJobRunning(String jobId) throws CondorExecException {
		boolean running = false;
		String statusCode = getJobStatusCode(jobId);
		if (statusCode != null && (statusCode.length() > 0) && statusCode.toLowerCase().contains("r")) {
			running = true;
		}
		return running;
	}

	/*
	 *  Determine if a job is in a suspended or held state.
	 */
	private boolean isJobHeld(String jobId) throws CondorExecException {
		boolean held = false;
		String statusCode = getJobStatusCode(jobId);
		if (statusCode != null && (statusCode.length() > 0) && statusCode.toLowerCase().contains("h")) {
			held = true;
		}
		return held;
	}
	
	
	/*
	 * Returns the status code for a particular job ID specified by the caller.
	 * The method determines this status code by executing "condor_q" and
	 * parsing the results.
	 */
	private String getJobStatusCode(String jobId) throws CondorExecException {
		String statusCode = null;
		
		// Set up the command to run, with arguments. The only argument in this
		// case is the job ID.
		String[] command = new String[2];
		command[0] = "condor_q";
		command[1] = jobId;

		int exitValue;
		try {
			Process condorQueue = Runtime.getRuntime().exec(command);
			exitValue = condorQueue.waitFor();

			if (exitValue == 0) {
				// Process the output of the command
		    	Reader reader = new InputStreamReader(condorQueue.getInputStream());
		    	BufferedReader bufReader = new BufferedReader(reader);
		    	String line = null;
		    	
		    	while ((line = bufReader.readLine()) != null) {
		    		// Ignore lines that don't begin with the job ID. These lines
		    		// are either just blank or contain the output header.
		    		line = line.trim();
		    		if (line.startsWith(jobId)) {
		    			Matcher matcher = condorQueueRegex.matcher(line);
		    			if (matcher.find()) {
		    				statusCode = matcher.group(6);
		    			}
		    		}
		    	}
		    	bufReader.close();
			} else {
				// The condor_q command failed completely...
				throw new CondorExecException("The condor_q command exited abnormally with exit value " + exitValue);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new CondorExecException("Interrupted.", e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new CondorExecException(e.getMessage(), e);
		}
		
		return statusCode;
	}
	
	/* 
	 * Confirm whether a particular job, specified with a job ID, is
	 * on the grid or not. We do NOT rely on our own DRMAA
	 * implementation to determine this.
	 */ 
	private boolean isJobPresent(String jobId) throws CondorExecException {
		boolean present = false;
		
		// Set up the command to run, with arguments. The only argument in this
		// case is the job ID.
		String[] command = new String[2];
		command[0] = "condor_q";
		command[1] = jobId;

		int exitValue;
		try {
			Process condorQueue = Runtime.getRuntime().exec("condor_q");
			exitValue = condorQueue.waitFor();

			if (exitValue == 0) {
		    	Reader reader = new InputStreamReader(condorQueue.getInputStream());
		    	BufferedReader bufReader = new BufferedReader(reader);
		    	String line = null;
		    	
		    	while ((line = bufReader.readLine()) != null) {
		    		// Ignore lines that don't begin with the job ID. These lines
		    		// are either just blank or contain the output header.
		    		line = line.trim();
		    		if (line.startsWith(jobId)) {
		    			present = true;
		    		}
		    	}
		    	bufReader.close();
			} else {
				// The 'condor_q' command failed completely...
				throw new CondorExecException("The 'condor_q' command exited abnormally with exit value " + exitValue);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new CondorExecException("Interrupted.", e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new CondorExecException(e.getMessage(), e);
		}
		
		return present;
	}
	
	/*
	 * Return a job template that is configured to execute /bin/sleep for 5 minutes
	 * on the grid. The job template is ready to be submitted through the session that
	 * is passed in as an argument.
	 */
	private JobTemplate getSleepJobTemplate(Session session) throws DrmaaException {
		JobTemplate jt = session.createJobTemplate();

		// Create a job that will sleep for 5 minutes
		jt.setRemoteCommand("/bin/sleep");
		jt.setArgs(Collections.singletonList("300"));
		
		// Set the job name
		jt.setJobName(name);
		
		return jt;
	}
	
	/*
	 * Remove a job from Condor by executing "condor_rm". We do this
	 * in order to remove the jobs that have been placed on the grid
	 * by running this test class. The method makes no attempt at
	 * checking whether the specified job ID exists or whether it is
	 * running or not. In fact, all errors are silently ignored because
	 * the method is just designed to do some cleanup after the tests
	 * are run...
	 */
	private static void removeJob(String jobId) {
		String[] removeCmd = new String[2];
		removeCmd[0] = "condor_rm";
		removeCmd[1] = jobId;

		try {
			Process condorQueue = Runtime.getRuntime().exec(removeCmd);
			int exitValue = condorQueue.waitFor();
			if (exitValue != 0) {
				System.err.println("Problem executing condor_rm.");
			}
		} catch (Exception e) {
			// ignored (see comments above)
		}
	}
	
	/**
	 * Runs after the testing is complete and removes the input and output files.
	 */
	@AfterClass
	public static void cleanup() {
		Iterator<String> iter = jobIdsToCleanup.iterator();
		while (iter.hasNext()) {
			String jobId = (String) iter.next();
			removeJob(jobId);
		}
	}
}
