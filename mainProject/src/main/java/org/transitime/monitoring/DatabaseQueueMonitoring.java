/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitime.monitoring;

import org.transitime.applications.Core;
import org.transitime.config.DoubleConfigValue;
import org.transitime.db.hibernate.DataDbLogger;
import org.transitime.utils.EmailSender;
import org.transitime.utils.StringUtils;

/**
 * For monitoring access to database. Examines size of the db logging queue
 * to make sure that writes are not getting backed up.
 *
 * @author SkiBu Smith
 *
 */
public class DatabaseQueueMonitoring extends MonitorBase {

	DoubleConfigValue maxQueueFraction = new DoubleConfigValue(
			"transitime.monitoring.maxQueueFraction", 
			0.4, 
			"If database queue fills up by more than this 0.0 - 1.0 "
			+ "fraction then database monitoring is triggered.");
	
	/********************** Member Functions **************************/

	/**
	 * Simple constructor
	 * 
	 * @param emailSender
	 * @param agencyId
	 */
	public DatabaseQueueMonitoring(EmailSender emailSender, String agencyId) {
		super(emailSender, agencyId);
	}

	/* (non-Javadoc)
	 * @see org.transitime.monitoring.MonitorBase#triggered()
	 */
	@Override
	protected boolean triggered() {
		Core core = Core.getInstance();
		if (core == null)
			return false;
		
		DataDbLogger dbLogger = core.getDbLogger();
		
		setMessage("Database queue fraction=" 
				+ StringUtils.twoDigitFormat(dbLogger.queueLevel())
				+ " while max allowed fraction=" 
				+ StringUtils.twoDigitFormat(maxQueueFraction.getValue()) 
				+ ", and items in queue=" + dbLogger.queueSize()
				+ ".",
				dbLogger.queueLevel());
		
		return dbLogger.queueLevel() > maxQueueFraction.getValue(); 
	}

	/* (non-Javadoc)
	 * @see org.transitime.monitoring.MonitorBase#type()
	 */
	@Override
	protected String type() {
		return "Database Queue";
	}
}
