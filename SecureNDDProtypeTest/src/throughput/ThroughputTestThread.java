package throughput;

import it.unisa.dia.gas.jpbc.Element;

import java.util.List;

import base.MyCountDown;
import util.MyCounter;

public class ThroughputTestThread extends Thread {

	private MyCountDown threadCounter;

	private List<Element> tArray;

	private RepositoryForThroughputTest repo;
	
	private MyCounter throughput;
	
	private Integer uid;
	
	private Integer repoNum;
	
	private long stTime;

	public ThroughputTestThread(String threadName, MyCountDown threadCounter, int uid, List<Element> tArray, RepositoryForThroughputTest repo, int repoNum, MyCounter throughput, long stTime) {

		super(threadName);

		this.threadCounter = threadCounter;
		this.tArray = tArray;
		this.repo = new RepositoryForThroughputTest(repo);
		this.throughput = throughput;
		
		this.uid = uid;
		this.repoNum = repoNum;
		this.stTime = stTime;
	}

	public void run() {

		System.out.println(getName() + " is running!");
		
		repo.secureSearch(uid, tArray, repoNum, throughput, stTime);
		
		System.out.println("inner throughput = " + throughput.getCtr());

		//System.out.println(getName() + " is finished! Number of candidate: " + resultInL.size());
		threadCounter.countDown();
	}
}
