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

import java.util.*;
import net.sf.igs.SessionImpl;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.JobTemplate;


/**
 * TODO: Use JUnit
 */
public class SimpleTest {

	/**
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			Session session = new SessionImpl();

			session.init("");
			JobTemplate jt = session.createJobTemplate();

			jt.setRemoteCommand("/bin/sleep");
			jt.setArgs(Collections.singletonList("120"));
			jt.setJobName("myname");
			String jobId = session.runJob(jt);
			System.out.println("Submitted job to Condor. Id: " + jobId);

		} catch (DrmaaException de) {
			de.printStackTrace();
		}
	}
}
