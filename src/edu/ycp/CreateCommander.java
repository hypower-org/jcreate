/**

Copyright (c) 2012 Patrick Martin <prof.pmartin10@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/

package edu.ycp;

import java.nio.*;
import java.util.concurrent.BlockingQueue;

import edu.ycp.comm.CreateHardwareManager;

public class CreateCommander implements Runnable {

	private final BlockingQueue<ByteBuffer> dataQueue;
	private final BlockingQueue<ByteBuffer> commandQueue;
	
	private boolean running = false;
	
	private Thread mainThread;
	
	public CreateCommander(BlockingQueue<ByteBuffer> dataQueue,
			BlockingQueue<ByteBuffer> commandQueue) {
		super();
		this.dataQueue = dataQueue;
		this.commandQueue = commandQueue;
		
		startUp();
	}

	private final void startUp(){
		mainThread = new Thread(this);
		mainThread.setName(CreateCommander.class.getSimpleName());
		mainThread.start();
		running = true;
	}

	public final void requestStop(){
		
	}
	
	@Override
	public void run() {
		while(running){
			try {
				ByteBuffer outBB = ByteBuffer.allocate(2);
				outBB.put((byte) 142);
				outBB.put((byte) 6);
				commandQueue.put(outBB);
				
				System.out.println("data queue remaining cap: " + dataQueue.remainingCapacity());
				ByteBuffer newBB = dataQueue.take();
				
				// process the new data buffer!
				
				System.out.print(Thread.currentThread().getName());
//				Thread.sleep(100);
				
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		
	}

	
	
}
