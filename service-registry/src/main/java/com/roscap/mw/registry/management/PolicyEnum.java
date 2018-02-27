package com.roscap.mw.registry.management;

import com.roscap.mw.registry.management.impl.ExclusiveElement;
import com.roscap.mw.registry.management.impl.RandomElement;
import com.roscap.mw.registry.management.impl.RoundRobin;

/**
 * enumeration of available instance management policies.
 * used for configuration
 * 
 * @author is.zharinov
 *
 */
public enum PolicyEnum {
	EXCLUSIVE(ExclusiveElement.class),
	RANDOM(RandomElement.class),
	ROUND_ROBIN(RoundRobin.class);
	
	public final Class<? extends InstanceManagementPolicy> policyClass;
	
	PolicyEnum(Class<? extends InstanceManagementPolicy> arg0) {
		policyClass = arg0;
	}
}
