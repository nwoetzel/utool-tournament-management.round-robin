package utool.plugin.roundrobin.tieresolver;

import utool.plugin.roundrobin.RoundRobinPlayer;

/**
 * This tie resolver simply returns the second parameter
 * @author Justin Kreier
 * @version 1/26/2013
 */
public class LazyTieResolver implements TieResolver{

	@Override
	public RoundRobinPlayer resolveTie(RoundRobinPlayer p1, RoundRobinPlayer p2) {
		return p2;
	}

}
