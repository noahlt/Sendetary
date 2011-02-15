import java.awt._
import java.awt.event._
import java.awt.geom._
import java.awt.font._
import java.awt.dnd._
import java.awt.datatransfer.DataFlavor
import java.io.StringReader
import java.io.ByteArrayOutputStream

/* This code is ridiculously ugly. */

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
    println("Hi there.");

    val wl:WindowListener = new WindowAdapter() {
      override def windowClosing(e: WindowEvent) { System.exit(0) }
      override def windowClosed(e: WindowEvent) { System.exit(0) }
    }

    val font = new Font("Vera Sans", Font.BOLD, 18)
    val f = new Frame("2D Text")
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
	  println("yes plain text is supported!")
	  val sr: StringReader = t.getTransferData(DataFlavor.plainTextFlavor)
				  .asInstanceOf[StringReader]
	  processString(stringFromReader(sr))
	}
      }

      def dragEnter(e: DropTargetDragEvent) { }

      def dragOver(e: DropTargetDragEvent) { }

      def dragExit(e: DropTargetEvent) { }

      def dropActionChanged(e: DropTargetDragEvent) { }

    })
  }
}
