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

import java.io.File;
import java.util.ArrayList;
import net.sf.igs.SessionImpl;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.JobTemplate;

// TODO: Use JUnit

public class BlastTest {

	/**
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			Session session = new SessionImpl();

			String home = System.getProperty("user.home");
			String sep = File.separator;
			String dataDir = home + sep + "storage_testing" + sep + "test_data" + sep;

			session.init("");
			JobTemplate jt = session.createJobTemplate();

			jt.setRemoteCommand("/usr/local/bin/blastall");
			ArrayList<String> jobArgs = new ArrayList<String>();
			jobArgs.add("-p");
			jobArgs.add("blastp");
			jobArgs.add("-i");
			jobArgs.add(dataDir + "medium.aa");
			jobArgs.add("-d");
			jobArgs.add(dataDir + "rel161.fsa.aa");
			jt.setArgs(jobArgs);
			jt.setOutputPath(":" + home + sep + "blast_drmaa.out");
			jt.setOutputPath(":" + home + sep + "blast_drmaa.err");

			jt.setJobName("myname");
			String jobId = session.runJob(jt);
			System.out.println("Submitted job to Condor. Id: " + jobId);

		} catch (DrmaaException de) {
			de.printStackTrace();
		}
	}
}
