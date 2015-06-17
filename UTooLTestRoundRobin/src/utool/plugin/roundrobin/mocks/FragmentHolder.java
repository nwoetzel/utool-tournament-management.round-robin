package utool.plugin.roundrobin.mocks;
import utool.plugin.observer.Observer;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;


public class FragmentHolder extends FragmentActivity implements Observer{
	public MockRoundFragment r;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		
		r=new MockRoundFragment();
	}

	public MockRoundFragment getRoundFragment()
	{
		return r;
	}

	@Override
	public void updateObserver(Object observedObject) {
		// TODO Auto-generated method stub
		
	}
}