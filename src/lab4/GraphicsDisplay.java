package lab4;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.JPanel;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import jdk.jfr.Description;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Stack;



@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {

    // Список координат точек для построения графика
    private Double[][] graphicsData;
    // Флаговые переменные, задающие правила отображения графика
    private boolean showAxis = true;
    private boolean showMarkers = true;
    // Границы диапазона пространства, подлежащего отображению
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    // Используемый масштаб отображения
    private double scale;

    private final Font gridFont = new Font(Font.SANS_SERIF, Font.ITALIC, 18);
    private final DecimalFormat formatterX = (DecimalFormat) NumberFormat.getInstance();
    private final DecimalFormat formatterY = (DecimalFormat) NumberFormat.getInstance();
    private final BasicStroke gridStroke = new BasicStroke(1f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,10f, new float[]{1}, 0f);
    private final BasicStroke gridStrokeMin = new BasicStroke(1f);;

    // Различные стили черчения линий
    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;
    // Различные шрифты отображения надписей
    private Font axisFont;
    private boolean showModifiedCondition = true;
    private boolean turnGraph = false;
    private boolean showGrid = false;

    private Double[][] graphicsDataOriginal;
    public Stack<Double[][]> undoLog = new Stack<>();
    private int selectedMarker = -1;
    private Double[][] viewport = new Double[2][2];
    Double[] originalPoint = new Double[2];
    Double[] finalPoint = new Double[2];
    boolean scaleMode = false;
    boolean changeMode = false;
    boolean changes = false;
    private final java.awt.geom.Rectangle2D.Double selectionRect = new java.awt.geom.Rectangle2D.Double();
    private static final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance();
    double activate = 1.25;
    private final BasicStroke selectionStroke;
    private final Font labelFont;
    double scaleX;
    double scaleY;
    public GraphicsDisplay() {
// Цвет заднего фона области отображения - белый
        setBackground(Color.WHITE);
// Сконструировать необходимые объекты, используемые в рисовании
// Перо для рисования графика
        graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f);
// Перо для рисования осей координат
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
// Перо для рисования контуров маркеров
        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
// Шрифт для подписей осей координат
        axisFont = new Font("Serif", Font.BOLD, 36);
        selectionStroke = new BasicStroke(1.0F, 0, 0, 10.0F, new float[]{10.0F, 10.0F}, 0.0F);
        labelFont = new Font(Font.SANS_SERIF, Font.ITALIC+Font.PLAIN, 20);
        addMouseListener(new MouseHandler());
        addMouseMotionListener(new MouseMotionHandler());
    }

    // Данный метод вызывается из обработчика элемента меню "Открыть файл с графиком"
    // главного окна приложения в случае успешной загрузки данных
    public void showGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData;
        graphicsDataOriginal = new Double[graphicsData.length][2];
        for(int i=0; i<graphicsData.length; i++) {
            graphicsDataOriginal[i][0]=graphicsData[i][0];
            graphicsDataOriginal[i][1]=graphicsData[i][1];
        }
        repaint();
    }

    // Методы-модификаторы для изменения параметров отображения графика
// Изменение любого параметра приводит к перерисовке области
    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }
    public void setTurnGraph(boolean turnGraph) {
        this.turnGraph = turnGraph;
        repaint();
    }
    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        repaint();
    }
    //для сохранения пределов нового окна

    public void zoomToRegion(double x1, double y1, double x2, double y2) {

        viewport[0][0]=x1;
        viewport[0][1]=y1;
        viewport[1][0]=x2;
        viewport[1][1]=y2;

        repaint();
    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graphicsData == null || graphicsData.length == 0) return;
        if (undoLog.size() == 0) {
            viewport[0][0] = graphicsData[0][0];
            viewport[1][0] = graphicsData[graphicsData.length - 1][0];
            viewport[1][1] = graphicsData[0][1];
            viewport[0][1] = viewport[1][1];

            for (int i = 1; i < graphicsData.length; i++) {
                if (graphicsData[i][1] < viewport[1][1]) {
                    viewport[1][1] = graphicsData[i][1];
                }
                if (graphicsData[i][1] > viewport[0][1]) {
                    viewport[0][1] = graphicsData[i][1];
                }
            }

            maxX = viewport[1][0];
            minX = viewport[0][0];
            maxY = viewport[0][1];
            minY = viewport[1][1];
        }

        if (turnGraph) {
            scaleX = getSize().getHeight() / (viewport[1][0] - viewport[0][0]);
            scaleY = getSize().getWidth() / (viewport[0][1] - viewport[1][1]);
            double scale = Math.min(scaleX, scaleY);

            if (scale == scaleY) {
                double xIncrement = (getSize().getHeight() / scaleX - (viewport[1][0] - viewport[0][0])) / 2;
                viewport[1][0] += xIncrement;
                viewport[0][0] -= xIncrement;
            }

            if (scale == scaleX) {
                double yIncrement = (getSize().getWidth() / scaleY - (viewport[0][1] - viewport[1][1])) / 2;
                viewport[0][1] += yIncrement;
                viewport[1][1] -= yIncrement;
            }
        } else {
            scaleX = getSize().getWidth() / (viewport[1][0] - viewport[0][0]);
            scaleY = getSize().getHeight() / (viewport[0][1] - viewport[1][1]);
            double scale = Math.min(scaleX, scaleY);

            if (scale == scaleY) {
                double xIncrement = (getSize().getWidth() / scaleX - (viewport[1][0] - viewport[0][0])) / 2;
                viewport[1][0] += xIncrement;
                viewport[0][0] -= xIncrement;
            }

            if (scale == scaleX) {
                double yIncrement = (getSize().getHeight() / scaleY - (viewport[0][1] - viewport[1][1])) / 2;
                viewport[0][1] += yIncrement;
                viewport[1][1] -= yIncrement;
            }
        }


// Шаг 7 - Сохранить текущие настройки холста
        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();
// Шаг 8 - В нужном порядке вызвать методы отображения элементов графика
// Порядок вызова методов имеет значение, т.к. предыдущий рисунок будет затираться последующим
// Первыми (если нужно) отрисовываются оси координат.
        if (turnGraph) rotatePanel(canvas);
        if (showAxis) paintAxis(canvas);
        paintGraphics(canvas);
        paintLabels(canvas);
        if (showMarkers) paintMarkers(canvas);
        if (showGrid) paintGrids(canvas);
        paintSelection(canvas);
// Шаг 9 - Восстановить старые настройки холста
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
    }

    public void setXDigits(int x){
        formatterX.setMaximumFractionDigits(x);
    }

    public void setYDigits(int y){
        formatterY.setMaximumFractionDigits(y);
    }

    // Метод отображения всего компонента, содержащего график

    protected void reset() {

        if (graphicsData != null)
            for (int i = 0; i < graphicsData.length; i++) {
                graphicsData[i][0] = graphicsDataOriginal[i][0];
                graphicsData[i][1] = graphicsDataOriginal[i][1];
            }
        undoLog.clear();

        zoomToRegion(minX,maxY,maxX,minY);
    }
    private void paintSelection(Graphics2D canvas) {
        if (scaleMode) {
            canvas.setStroke(selectionStroke);
            canvas.setColor(Color.black);
            canvas.draw(selectionRect);
        }
    }
    // Отрисовка графика по прочитанным координатам
    protected void paintGraphics(Graphics2D canvas) {
        // Задать паттерн для длины сегментов линии и расстояния между ними
        float segment1Length = 20.0f;
        float segment2Length = 10.0f;
        float segment3Length = 5.0f;
        float gapLength = 5.0f;
        float[] dashPattern = {segment1Length, gapLength, segment3Length, gapLength, segment2Length, gapLength, segment3Length, gapLength};
        float dashPhase = 0.0f;
        // Создать объект BasicStroke с заданным паттерном
        BasicStroke graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, dashPattern, dashPhase);

        // Выбрать линию для рисования графика
        canvas.setStroke(graphicsStroke);

        // Выбрать цвет линии
        canvas.setColor(Color.RED);

    /* Будем рисовать линию графика как путь, состоящий из множества
    сегментов (GeneralPath). Начало пути устанавливается в первую точку
    графика, после чего прямой соединяется со следующими точками */
        GeneralPath graphics = new GeneralPath();

        for (int i = 0; i < graphicsData.length; i++) {
            // Преобразовать значения (x,y) в точку на экране point
            Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);

            if (i > 0) {
                // Не первая итерация – вести линию в точку point
                graphics.lineTo(point.getX(), point.getY());
            } else {
                // Первая итерация - установить начало пути в точку point
                graphics.moveTo(point.getX(), point.getY());
            }
        }

        // Отобразить график
        canvas.draw(graphics);
    }

    // Отображение маркеров точек, по которым рисовался график
    protected void paintMarkers(Graphics2D canvas) {
        canvas.setStroke(markerStroke);

        canvas.setColor(Color.RED);
        int i = 0;
        for (Double[] point : graphicsData) {
            GeneralPath marker = new GeneralPath();
            if (showModifiedCondition && point[1].intValue() % 2 == 0 && point[1].intValue()!= 0)
                canvas.setColor(Color.MAGENTA);
            else
                canvas.setPaint(Color.BLACK);
            Point2D.Double center = xyToPoint(point[0],point[1]);
            if (selectedMarker >= 0 && i == selectedMarker) {
                marker.moveTo(center.getX() + activate*2.75, center.getY() - activate*5);
                marker.lineTo(marker.getCurrentPoint().getX() - activate*5.5, marker.getCurrentPoint().getY());
                marker.moveTo(marker.getCurrentPoint().getX(), marker.getCurrentPoint().getY() + activate*10);
                marker.lineTo(marker.getCurrentPoint().getX() + activate*5.5, marker.getCurrentPoint().getY());
                marker.moveTo(center.getX(), marker.getCurrentPoint().getY());
                marker.lineTo(marker.getCurrentPoint().getX(), marker.getCurrentPoint().getY() - activate*10);
                marker.moveTo(center.getX() - activate*5, center.getY() + activate*2.75);
                marker.lineTo(marker.getCurrentPoint().getX(), marker.getCurrentPoint().getY() - activate*5.5);
                marker.moveTo(marker.getCurrentPoint().getX() + activate*10, marker.getCurrentPoint().getY());
                marker.lineTo(marker.getCurrentPoint().getX(), marker.getCurrentPoint().getY() + activate*5.5);
                marker.moveTo(marker.getCurrentPoint().getX(), center.getY());
                marker.lineTo(marker.getCurrentPoint().getX() - activate*10, marker.getCurrentPoint().getY());
                canvas.setColor(Color.GREEN);
            }
            else {
                marker.moveTo(center.getX() + 2.75, center.getY() - 5);
                marker.lineTo(marker.getCurrentPoint().getX() - 5.5, marker.getCurrentPoint().getY());
                marker.moveTo(marker.getCurrentPoint().getX(), marker.getCurrentPoint().getY() + 10);
                marker.lineTo(marker.getCurrentPoint().getX() + 5.5, marker.getCurrentPoint().getY());
                marker.moveTo(center.getX(), marker.getCurrentPoint().getY());
                marker.lineTo(marker.getCurrentPoint().getX(), marker.getCurrentPoint().getY() - 10);
                marker.moveTo(center.getX() - 5, center.getY() + 2.75);
                marker.lineTo(marker.getCurrentPoint().getX(), marker.getCurrentPoint().getY() - 5.5);
                marker.moveTo(marker.getCurrentPoint().getX() + 10, marker.getCurrentPoint().getY());
                marker.lineTo(marker.getCurrentPoint().getX(), marker.getCurrentPoint().getY() + 5.5);
                marker.moveTo(marker.getCurrentPoint().getX(), center.getY());
                marker.lineTo(marker.getCurrentPoint().getX() - 10, marker.getCurrentPoint().getY());
            }
            canvas.draw(marker);
            i++;
        }
        repaint();
    }

    // Метод, обеспечивающий отображение осей координат
    protected void paintAxis(Graphics2D canvas) {
// Установить особое начертание для осей
        canvas.setStroke(axisStroke);
// Оси рисуются чѐрным цветом
        canvas.setColor(Color.BLACK);
// Стрелки заливаются чѐрным цветом
        canvas.setPaint(Color.BLACK);
// Подписи к координатным осям делаются специальным шрифтом
        canvas.setFont(axisFont);
// Создать объект контекста отображения текста - для получения характеристик устройства (экрана)
        FontRenderContext context = canvas.getFontRenderContext();
        if (viewport[0][0] <= 0.0 && viewport[1][0] >= 0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(0, viewport[0][1]), xyToPoint(0, viewport[1][1])));
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(0, viewport[0][1]);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() + 5, arrow.getCurrentPoint().getY() + 20);
            arrow.lineTo(arrow.getCurrentPoint().getX() - 10, arrow.getCurrentPoint().getY());
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);
            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, viewport[0][1]);
            canvas.drawString("y", (float) labelPos.getX() + 10, (float) (labelPos.getY() - bounds.getY()));
        }
        if (viewport[1][1] <= 0.0 && viewport[0][1] >= 0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(viewport[0][0], 0), xyToPoint(viewport[1][0], 0)));
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(viewport[1][0], 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() - 20, arrow.getCurrentPoint().getY() - 5);
            arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY() + 10);
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);
            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(viewport[1][0], 0);
            canvas.drawString("x", (float) (labelPos.getX() - bounds.getWidth() - 10), (float) (labelPos.getY() + bounds.getY()));
        }
    }

    /* Метод-помощник, осуществляющий преобразование координат.
    * Оно необходимо, т.к. верхнему левому углу холста с координатами
    * (0.0, 0.0) соответствует точка графика с координатами (minX, maxY),
    где
    * minX - это самое "левое" значение X, а
    * maxY - самое "верхнее" значение Y.
    */


    /* Метод-помощник, возвращающий экземпляр класса Point2D.Double
     * смещѐнный по отношению к исходному на deltaX, deltaY
     * К сожалению, стандартного метода, выполняющего такую задачу, нет.
     */
    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
// Инициализировать новый экземпляр точки
        Point2D.Double dest = new Point2D.Double();
// Задать еѐ координаты как координаты существующей точки + заданные смещения
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }
    protected void rotatePanel(Graphics2D canvas){
        canvas.translate(0, getHeight());
        canvas.rotate(-Math.PI/2);
    }
    protected void paintGrids(Graphics2D canvas){
        canvas.setFont(gridFont);
        FontRenderContext context = canvas.getFontRenderContext();
        canvas.setColor(Color.gray);
        double currentValueX=0;
        double currentValueY=0;
        double incrementX = (maxX-minX)/20;
        double incrementY = (maxY-minY)/20;
        double incrementXIn = Double.parseDouble(formatter.format(incrementX).replace(',', '.'))/10;
        double incrementYIn = Double.parseDouble(formatter.format(incrementY).replace(',', '.'))/10;
        double currentValueXModified = -Double.parseDouble(formatter.format(incrementX).replace(',', '.'));
        double currentValueYModified = -Double.parseDouble(formatter.format(incrementY).replace(',', '.'));
        int counter;
        double currentValueXIn;
        double currentValueYIn;

        while((currentValueX<maxX || currentValueY<maxY) || (-currentValueX>minX || -currentValueY>minY)) {
            canvas.setStroke(gridStroke);
            String formattedDoubleX = formatter.format(currentValueX);
            String formattedDoubleY = formatter.format(currentValueY);
            canvas.draw(new Line2D.Double(xyToPoint(currentValueX,minY),xyToPoint(currentValueX,maxY)));
            canvas.draw(new Line2D.Double(xyToPoint(-currentValueX,minY),xyToPoint(-currentValueX,maxY)));
            canvas.draw(new Line2D.Double(xyToPoint(minX,currentValueY),xyToPoint(maxX,currentValueY)));
            canvas.draw(new Line2D.Double(xyToPoint(minX,-currentValueY),xyToPoint(maxX,-currentValueY)));
            Rectangle2D boundsX = gridFont.getStringBounds(formattedDoubleX,context);
            Rectangle2D boundsY = gridFont.getStringBounds(formattedDoubleY,context);
            Point2D.Double labelPosXRight = xyToPoint(-currentValueXModified,0);
            Point2D.Double labelPosXLeft = xyToPoint(currentValueXModified,0);
            Point2D.Double labelPosYUp = xyToPoint(0,currentValueY);
            Point2D.Double labelPosYDown = xyToPoint(0,currentValueYModified);
            canvas.drawString(formatter.format(-currentValueXModified),(float)(labelPosXRight.getX()-15),
                    (float)(labelPosXRight.getY())-5);
            canvas.drawString(formatter.format(currentValueXModified),(float)(labelPosXLeft.getX()-boundsX.getX()-15),
                    (float)(labelPosXLeft.getY())-5);
            canvas.drawString(formatter.format(currentValueY),(float)(labelPosYUp.getX()-boundsY.getX()+1),
                    (float)(labelPosYUp.getY()-boundsY.getY()));
            canvas.drawString(formatter.format(currentValueYModified),(float)(labelPosYDown.getX()-boundsY.getX()+10),
                    (float)(labelPosYDown.getY()-boundsY.getY()));
            currentValueYIn=0;
            counter=0;
            canvas.setStroke(gridStrokeMin);

            while(currentValueYIn<=maxY || -currentValueYIn>minY) {
                if ((counter+15)%10==0) {
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(currentValueX, currentValueYIn), -6, 0),
                            shiftPoint(xyToPoint(currentValueX, currentValueYIn), 6, 0)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(currentValueX, -currentValueYIn), -6, 0),
                            shiftPoint(xyToPoint(currentValueX, -currentValueYIn), 6, 0)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(-currentValueX, currentValueYIn), -6, 0),
                            shiftPoint(xyToPoint(-currentValueX, currentValueYIn), 6, 0)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(-currentValueX, -currentValueYIn), -6, 0),
                            shiftPoint(xyToPoint(-currentValueX, -currentValueYIn), 6, 0)));
                }
                else
                {
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(currentValueX, currentValueYIn),-3,0),
                            shiftPoint(xyToPoint(currentValueX, currentValueYIn),3,0)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(currentValueX, -currentValueYIn),-3,0),
                            shiftPoint(xyToPoint(currentValueX, -currentValueYIn),3,0)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(-currentValueX, currentValueYIn), -3, 0),
                            shiftPoint(xyToPoint(-currentValueX, currentValueYIn), 3, 0)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(-currentValueX, -currentValueYIn), -3, 0),
                            shiftPoint(xyToPoint(-currentValueX, -currentValueYIn), 3, 0)));
                }
                counter++;
                currentValueYIn += incrementYIn;
            }

            counter = 0;
            currentValueXIn = 0;
            while(currentValueXIn <= maxX || -currentValueXIn > minX) {
                if ((counter+15)%10==0) {
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(currentValueXIn, currentValueY), 0, -6),
                            shiftPoint(xyToPoint(currentValueXIn, currentValueY), 0, 6)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(currentValueXIn, -currentValueY), 0, -6),
                            shiftPoint(xyToPoint(currentValueXIn, -currentValueY), 0, 6)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(-currentValueXIn, currentValueY), 0, -6),
                            shiftPoint(xyToPoint(-currentValueXIn, currentValueY), 0, 6)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(-currentValueXIn, -currentValueY), 0, -6),
                            shiftPoint(xyToPoint(-currentValueXIn, -currentValueY), 0, 6)));
                }
                else
                {
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(currentValueXIn, currentValueY), 0, -3),
                            shiftPoint(xyToPoint(currentValueXIn, currentValueY), 0, 3)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(currentValueXIn, -currentValueY), 0, -3),
                            shiftPoint(xyToPoint(currentValueXIn, -currentValueY), 0, 3)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(-currentValueXIn, currentValueY), 0, -3),
                            shiftPoint(xyToPoint(-currentValueXIn, currentValueY), 0, 3)));
                    canvas.draw(new Line2D.Double(shiftPoint(xyToPoint(-currentValueXIn, -currentValueY), 0, -3),
                            shiftPoint(xyToPoint(-currentValueXIn, -currentValueY), 0, 3)));
                }
                counter++;
                currentValueXIn+=incrementXIn;
            }

            currentValueY += Double.parseDouble(formatter.format(incrementY).replace(',', '.'));
            currentValueX += Double.parseDouble(formatter.format(incrementX).replace(',', '.'));
            currentValueXModified -= Double.parseDouble(formatter.format(incrementX).replace(',', '.'));
            currentValueYModified -= Double.parseDouble(formatter.format(incrementY).replace(',', '.'));
        }
    }
    protected void paintLabels(Graphics2D canvas) {
        if (selectedMarker>=0) {
            formatter.setMaximumFractionDigits(undoLog.size()+(int)Math.ceil(viewport[1][0]));
            String label;
            canvas.setColor(Color.blue.brighter().brighter());
            Point2D.Double point = xyToPoint(graphicsData[selectedMarker][0], graphicsData[selectedMarker][1]);
            label = "X = " + formatter.format(graphicsData[selectedMarker][0]) + "; Y = " + formatter.format(graphicsData[selectedMarker][1]);
            canvas.setFont(labelFont);
            FontRenderContext context = canvas.getFontRenderContext();
            Rectangle2D bounds = labelFont.getStringBounds(label, context);
            if ((graphicsData[selectedMarker][0] <= viewport[1][0] &&
                    graphicsData[selectedMarker][0] >= (viewport[1][0]-(viewport[1][0]-viewport[0][0])/2))
                    && (graphicsData[selectedMarker][1] <= viewport[0][1]
                    && graphicsData[selectedMarker][1] >= (viewport[0][1]-(viewport[0][1]-viewport[1][1])/2)))
                canvas.drawString(label, (float)(point.getX() - bounds.getWidth()), (float)(point.getY() + bounds.getHeight()));
            else if((graphicsData[selectedMarker][0] <= viewport[1][0] &&
                    graphicsData[selectedMarker][0] >= (viewport[1][0]-(viewport[1][0]-viewport[0][0])/2))
                    && (graphicsData[selectedMarker][1]) >= viewport[1][1] && graphicsData[selectedMarker][1]<=(viewport[1][1]-(viewport[1][1]-viewport[0][1])/2))
                canvas.drawString(label, (float)(point.getX() - bounds.getWidth()), (float)(point.getY() - bounds.getHeight()));
            else if((graphicsData[selectedMarker][1]) >= viewport[1][1] && graphicsData[selectedMarker][1]<=(viewport[1][1]-(viewport[1][1]-viewport[0][1])/2)
                    && (graphicsData[selectedMarker][0] >= viewport[0][0] && graphicsData[selectedMarker][0]<=viewport[0][0]+(viewport[1][0]-viewport[0][0])/2))
                canvas.drawString(label, (float)(point.getX() + 5.0), (float)(point.getY() - bounds.getHeight()));

            else {
                canvas.drawString(label, (float)(point.getX() + 5.0), (float)(point.getY() + bounds.getHeight()));
            }
        }
    }

    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - viewport[0][0];
        double deltaY = viewport[0][1] - y;
        return new Point2D.Double(deltaX * scaleX, deltaY * scaleY);
    }

    protected Double[] pointToXY(int x, int y) {
        return new Double[]{viewport[0][0] + (double)x/scaleX, viewport[0][1]-(double)y/scaleY};
    }

    protected int findPoint(int x, int y) {
        if (graphicsData != null) {
            int pos = 0;
            double distance;
            for (Double[] point : graphicsData) {
                Point2D.Double screenPoint = xyToPoint(point[0], point[1]);
                if (!turnGraph)
                    distance = (screenPoint.getX() - x) * (screenPoint.getX() - x) +
                            (screenPoint.getY() - y) * (screenPoint.getY() - y);
                else
                    distance = (screenPoint.getX() - y) * (screenPoint.getX() - y) +
                            (screenPoint.getY() - x) * (screenPoint.getY() - x);

                if (distance < 100.0)
                    return pos;
                pos++;
            }
        }
        return -1;
    }

    public Double[][] getGraphicsData(){
        return graphicsData;
    }

    public class MouseHandler extends MouseAdapter {

        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == 3) {
                if (!undoLog.isEmpty())
                    viewport = undoLog.pop();
                else
                    zoomToRegion(viewport[0][0],viewport[0][1],viewport[1][0],viewport[1][1]);
                repaint();
            }
        }
        @Description("Для забора начальных координат при приближении")
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == 1) {
                if (!turnGraph) {
                    originalPoint = pointToXY(e.getX(), e.getY());
                    selectedMarker = findPoint(e.getX(), e.getY());
                }
                else {
                    originalPoint = pointToXY(getHeight() - e.getY(), e.getX());
                    selectedMarker = findPoint(e.getX(), getHeight()-e.getY());
                }
                if (selectedMarker >= 0) {
                    changeMode = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                } else {
                    scaleMode = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                    if (!turnGraph)
                        selectionRect.setFrame(e.getX(), e.getY(), 0.5D, 0.5D);
                    else
                        selectionRect.setFrame(getHeight() - e.getY(), e.getX(), 0.5D, 0.5D);
                }
            }
        }

        @Description("Для забора координат при приближении")
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == 1) {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                if (changeMode){
                    changeMode = false;
                    selectedMarker = -1;
                    repaint();
                }
                else {
                    scaleMode = false;
                    if (!turnGraph)
                        finalPoint = pointToXY(e.getX(), e.getY());
                    else
                        finalPoint = pointToXY(getHeight()-e.getY(),e.getX());
                    undoLog.add(viewport);
                    viewport = new Double[2][2];
                    zoomToRegion(originalPoint[0], originalPoint[1], finalPoint[0], finalPoint[1]);
                }
            }
        }

    }

    public class MouseMotionHandler implements MouseMotionListener {

        public void mouseDragged(MouseEvent e) {
            if (changeMode) {
                if (!turnGraph) {
                    Double[] currentPoint = pointToXY(e.getX(), e.getY());
                    double newY = currentPoint[1];
                    if (newY > viewport[0][1])
                        newY = viewport[0][1];
                    if (newY < viewport[1][1])
                        newY = viewport[1][1];
                    graphicsData[selectedMarker][1] = newY;
                } else {
                    Double[] currentPoint = pointToXY(getHeight() - e.getY(), e.getX());
                    double newX = currentPoint[0];
                    if (newX < viewport[0][0])
                        newX = viewport[0][0];
                    if (newX > viewport[1][0])
                        newX = viewport[1][0];
                    graphicsData[selectedMarker][0] = newX;
                }
                changes = true;
            } else {
                double width;
                double height;
                if (turnGraph) {
                    width = getHeight() - e.getY() - selectionRect.getX();
                    height = e.getX() - selectionRect.getY();
                } else {
                    width = e.getX() - selectionRect.getX();
                    height = e.getY() - selectionRect.getY();
                }
                selectionRect.setFrame(selectionRect.getX(), selectionRect.getY(), width, height);
            }
            repaint();
        }

        public void mouseMoved(MouseEvent e) {

            if (!turnGraph)
                selectedMarker = findPoint(e.getX(), e.getY());
            else {
                selectedMarker = findPoint(e.getX(), getHeight() - e.getY());
            }

            if (selectedMarker >= 0)
                setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            else
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            repaint();
        }
    }
}
