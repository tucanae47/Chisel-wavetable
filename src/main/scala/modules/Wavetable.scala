package modules

import chisel3._
import chisel3.experimental._ // To enable experimental features

class Timer(val w: Int) extends Module {
	val io = IO(new Bundle{
		val period = Input(UInt(w.W))
		val fire = Output(Bool())
	})

	val cnt = Counter.counter(io.period, Bool(true))
	io.fire := RegNext(cnt === io.period)
}

//TODO: this should stop at the end of the waveform
class Wavetable extends Module {
	val io = IO(new Bundle{
		val RBANK = Output(UInt(2.W))
		val RADDR = Output(UInt(8.W))
		val RCLK = Output(Bool())
		val En = Input(Bool())
		val freq = Input(UInt(13.W))
		val step = Input(UInt(3.W))
	})

	withReset(io.En){
		var periodTimer = Module(new Timer(16))
		periodTimer.io.period := io.freq

		val sampleCounter = Module(new Counter(1023))
	    sampleCounter.io.inc := periodTimer.io.fire
	    sampleCounter.io.amt := io.step

		io.RADDR := sampleCounter.io.tot(7, 0)
		io.RBANK := sampleCounter.io.tot(9, 8)

		io.RCLK := RegNext(periodTimer.io.fire)
	}
}