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
import java.io.File;
import java.io.FileReader;
import java.util.Collections;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests whether support for the setErrorPath() method really works.
 */
public class ErrorPathTest {
	private static String name = ErrorPathTest.class.getSimpleName();
	private static File errorFile = null;
	private static String errorPath;
	
	@BeforeClass
	public static void setup() {
		// Let's configure the error path for where the STDERR of the job should be saved to.
		errorPath = System.getProperty("user.home") + File.separator + name + ".err";
		
		errorFile = new File(errorPath);
		
		deleteErrorFile();
	}

	private static void deleteErrorFile() {
		// Delete the file if it's there (could be a leftover from previous tests)
		if (errorFile.exists()) {
			errorFile.delete();	
		}
	}
	
	@Test
	public void testErrorPath() {

		try {
			Session session = SessionFactory.getFactory().getSession();
			session.init("");
			JobTemplate jt = session.createJobTemplate();

			String badArgument = "abcdefg";
			
			jt.setRemoteCommand("/bin/sleep");
			jt.setArgs(Collections.singletonList(badArgument));
			
			// Set the job name
			jt.setJobName(name);

			// Make sure we don't have the file around from previous test invocations
			assertFalse(errorFile.exists());
			
			jt.setErrorPath(":" + errorPath);
			String jobId = session.runJob(jt);
			assertNotNull(jobId);
			assertTrue(jobId.length() > 0);
			
			// Wait for the job to complete (wait indefinitely)
			session.synchronize(Collections.singletonList(jobId), Session.TIMEOUT_WAIT_FOREVER, true);

			// Exit the session
			session.exit();
			
			// Now check that the STDERR file is actually there
			assertTrue(errorFile.exists());
			
			// More detailed checking. Actually see if the error has the right information in it.
			boolean correctFailure = false;
			BufferedReader reader = new BufferedReader(new FileReader(errorFile));
			String line = reader.readLine();
			if (line.contains(badArgument)) {
				correctFailure = true;
			}
			assertTrue(correctFailure);
			
		} catch (DrmaaException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void cleanup() {
		deleteErrorFile();
	}
}
