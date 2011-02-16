import java.awt._
import java.awt.event._
import java.awt.geom._
import java.awt.font._
import java.awt.dnd._
import java.awt.datatransfer.DataFlavor
import java.io.StringReader
import java.io.ByteArrayOutputStream

import org.jivesoftware.smack.{XMPPConnection, SASLAuthentication, PacketListener}
import org.jivesoftware.smack.packet.{Message, Packet}
import org.jivesoftware.smack.filter.PacketFilter

/* This code is ridiculously ugly. Don't judge me yet. */

class SendetaryCanvas extends Canvas {
  def SendetaryCanvas() {
    setBackground(Color.white);
  }

  override def paint(g: Graphics) {
    val g2:Graphics2D = g.asInstanceOf[Graphics2D]
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON)
    g2.setRenderingHint(RenderingHints.KEY_RENDERING,
			RenderingHints.VALUE_RENDER_QUALITY)
    
    val frc = g2.getFontRenderContext()
    val font = new Font("Vera Sans", Font.BOLD, 24)
    val tl = new TextLayout("Hello, world!", font, frc)
    val canvasSize = getSize()
    g2.setColor(Color.black)
    tl.draw(g2, canvasSize.width/30, canvasSize.height/2)
  }
}

// FIXME change all this to an actor?
class SendetaryJabber(val user: String, val host: String) {
  val conn = new XMPPConnection(host)
  conn.connect()
  /* Hack: For some reason it tries to use MD5 even when we set PLAIN
   * to high priority.  So tell it to not try MD5 at all: */
  SASLAuthentication.unsupportSASLMechanism("DIGEST-MD5")
  SASLAuthentication.supportSASLMechanism("PLAIN", 0)
  conn.login(user, "anagram")
  println("authenticated!")

  conn.addPacketListener(
    new PacketListener() {
      override def processPacket(packet: Packet) {
	packet match {
	  case m: Message => {
	    // FIXME I'm sure there's a Scala-tastic way to do this
	    // with real pattern matching instead of lame if-elses...
	    val s = m.getBody()
	    if (s.substring(0,7) == "http://") {
	      Runtime.getRuntime().exec("chrome --new-window " + s)
	      println("Received URL: " + s)
	    } else {
	      println(s)
	    }
	  }
	  case other => println("just another packet...")
	}
      }
    },
    new PacketFilter() {
      override def accept(packet: Packet) = true
    }
  )
  
  // This is temporary.
  def send(to: String, s: String) {
    val mesg = new Message(to + "@" + host)
    mesg.setBody(s)
    conn.sendPacket(mesg)
    println("[ sent IM to " + to + " ]")
  }
}

object Sendetary {

  // Gross.
  def stringFromReader(sr: StringReader): String =  {
    val buf = new ByteArrayOutputStream()
    var result = sr.read()
    while (result != -1) {
      buf.write(result.asInstanceOf[Byte])
      result = sr.read()
    }
    return buf.toString()
  }

  def processString(s: String) {
    if (s.substring(0,7) == "http://") {
      println("it's a website!!!")
      Runtime.getRuntime().exec("chrome --new-window " + s)
    } else {
      println(s)
    }
  }

  def main(args: Array[String]) {
    val us = args(0)
    val them = args(1)
    println(us)
    println(them)
    val jabber = new SendetaryJabber(us, "scorpio")

    val wl:WindowListener = new WindowAdapter() {
      override def windowClosing(e: WindowEvent) { System.exit(0) }
      override def windowClosed(e: WindowEvent) { System.exit(0) }
    }

    val f = new Frame("drag into this window to send to " + them)
    val canvas = new SendetaryCanvas()

    f.addWindowListener(wl)
    f.add("Center", canvas)
    f.pack()
    f.setSize(new Dimension(400, 300))
    f.show()

    val dt = new DropTarget(canvas, new DropTargetListener() {
      def drop(e: DropTargetDropEvent) {
	println("drop event!!!")
	e.acceptDrop(DnDConstants.ACTION_COPY)
	val t = e.getTransferable()
	/* DataFlavor.plainTextFlavor is deprecated, but the new way
	 * (which allows for Unicode etc) is complicated, so for
	 * version 1 we're using the deprecated version */
	if (t.isDataFlavorSupported(DataFlavor.plainTextFlavor)) {
	  val sr: StringReader = t.getTransferData(DataFlavor.plainTextFlavor)
				  .asInstanceOf[StringReader]
	  val s = stringFromReader(sr)
	  jabber.send(them, s)
	}
      }

      def dragEnter(e: DropTargetDragEvent) { }

      def dragOver(e: DropTargetDragEvent) { }

      def dragExit(e: DropTargetEvent) { }

      def dropActionChanged(e: DropTargetDragEvent) { }

    })
  }
}
