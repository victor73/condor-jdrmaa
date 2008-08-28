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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.ggf.drmaa.AlreadyActiveSessionException;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.InvalidContactStringException;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;
import org.junit.Test;

/**
 * Test DRMAA sessions.
 */
public class SessionTest {

	/**
	 * Test session initialization.
	 */
	@Test
	public void sessionInitializationTest() {
		Session session = null;
		try {
			session = SessionFactory.getFactory().getSession();
			assertNotNull(session);
			
			session.init("session_name");
			assertNotNull(session);
		} catch (DrmaaException de) {
			de.printStackTrace();
			fail(de.getMessage());
		} finally {
			try {
				if (session != null) {
					session.exit();
					session = null;
				}
			} catch (DrmaaException e) {
				// ignored
			}
		}
	}
	
	@Test(expected=AlreadyActiveSessionException.class)
	public void testInitWithActiveSession() throws DrmaaException {
		Session session = null;
		session = SessionFactory.getFactory().getSession();
		assertNotNull(session);
		try {
			session.init("session_name");
			
			session.init("session_name");
		} catch (DrmaaException e) {
			throw e;
		} finally {
			try {
				if (session != null) {
					session.exit();
					session = null;
				}
			} catch (DrmaaException e) {
				// ignored
			}
		}
	}
	
	/**
	 * Test whether a contact of null is allowed when initializing a
	 * session. In this DRMAA implementation, a null is not allowed
	 * and should cause an IllegalArgumentException
	 * @throws DrmaaException 
	 */
	@Test(expected=InvalidContactStringException.class)
	public void nullSessionInit() throws DrmaaException {
		Session session = SessionFactory.getFactory().getSession();
		assertNotNull(session);

		// Can we have a null session?
		try {
			session.init(null);
		} catch (DrmaaException e) {
			throw e;
		} finally {
			try {
				if (session != null) {
					session.exit();
					session = null;
				}
			} catch (DrmaaException e) {
				// ignored
			}
		}
	}
	
	
	/**
	 * Test the getContact() method before session initialization.
	 */
	@Test
	public void getContactBeforeInitTest() {
		Session session = null;
		try {
			session = SessionFactory.getFactory().getSession();
			
			String contact = session.getContact();
			assertNotNull(contact);
			assertTrue(contact.length() > 0);

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} finally {
			try {
				if (session != null) {
					session.exit();
					session = null;
				}
			} catch (DrmaaException e) {
				// ignored
			}
		}
	}

	/**
	 * Test the getContact() method after session initialization.
	 */
	@Test
	public void getContactAfterInitTest() {
		String sessionName = "abcde";
		Session session = null;
		try {
			session = SessionFactory.getFactory().getSession();
			
			session.init(sessionName);
			assertNotNull(session);
			
			String contact = session.getContact();
			assertNotNull(contact);
			assertTrue(contact.length() > 0 );
			assertEquals(sessionName, contact);
			
			//Exit the session
			session.exit();	
		} catch (DrmaaException de) {
			de.printStackTrace();
			fail(de.getMessage());
		} finally {
			try {
				if (session != null) {
					session.exit();
					session = null;
				}
			} catch (DrmaaException e) {
				// ignored
			}
		}
	}

	/**
	 * Test the getDrmSystem() method.
	 * 
	 * @see "DRMAA 1.0 specification."
	 */
	@Test
	public void drmSystemTest() {
		Session session = null;
		try {
			session = SessionFactory.getFactory().getSession();
			assertNotNull(session);
			
			session.init("session_name");
			assertNotNull(session);
			
			String system = session.getDrmSystem();
			assertNotNull(system);
			assertTrue(system.equalsIgnoreCase("condor"));

		} catch (DrmaaException de) {
			de.printStackTrace();
			fail(de.getMessage());
		} finally {
			try {
				if (session != null) {
					session.exit();
					session = null;
				}
			} catch (DrmaaException e) {
				// ignored
			}
		}
	}
	
	/**
	 * Test the getDrmaaImplementation method.
	 * 
	 * @see "DRMAA 1.0 specification"
	 */
	@Test
	public void drmImplementationTest() {
		Session session = null;
		try {
			session = SessionFactory.getFactory().getSession();
			assertNotNull(session);

			session.init("session_name");
			assertNotNull(session);

			String implementation = session.getDrmaaImplementation();

			assertNotNull(implementation);
			assertTrue(implementation.equalsIgnoreCase("condor"));

		} catch (DrmaaException de) {
			de.printStackTrace();
			fail(de.getMessage());
		} finally {
			try {
				if (session != null) {
					session.exit();
					session = null;
				}
			} catch (DrmaaException e) {
				// ignored
			}
		}

	}
	
}
