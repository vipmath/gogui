//----------------------------------------------------------------------------
// $Id$
// $Source$
//----------------------------------------------------------------------------

package net.sf.gogui.gtpdummy;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;
import net.sf.gogui.go.GoPoint;
import net.sf.gogui.gtp.GtpCallback;
import net.sf.gogui.gtp.GtpCommand;
import net.sf.gogui.gtp.GtpEngine;
import net.sf.gogui.gtp.GtpError;
import net.sf.gogui.version.Version;

//----------------------------------------------------------------------------

/** Dummy Go program for testing GTP controlling programs.
    See the GtpDummy documentation for information about the extension
    commands.
*/
public class GtpDummy
    extends GtpEngine
{
    public GtpDummy(PrintStream log, boolean useRandomSeed, long randomSeed,
                    int resign)
        throws Exception
    {
        super(log);
        registerCommands();
        setName("GtpDummy");
        setVersion(Version.get());
        m_random = new Random();
        m_resign = resign;
        if (useRandomSeed)
            m_random.setSeed(randomSeed);
        initSize(GoPoint.DEFAULT_SIZE);
        m_thread = Thread.currentThread();
    }

    public void cmdBWBoard(GtpCommand cmd)
    {        
        cmd.getResponse().append('\n');
        for (int x = 0; x < m_size; ++x)
        {
            for (int y = 0; y < m_size; ++y)
            {
                cmd.getResponse().append(m_random.nextBoolean() ? 'B' : 'W');
                if (y < m_size - 1)
                    cmd.getResponse().append(' ');
            }
            cmd.getResponse().append('\n');
        }                    
    }

    public void cmdBoardsize(GtpCommand cmd) throws GtpError
    {
        cmd.checkNuArg(1);
        int size = cmd.getIntArg(0, 1, GoPoint.MAXSIZE);
        initSize(size);
    }

    public void cmdCrash(GtpCommand cmd)
    {        
        System.err.println("Aborting GtpDummy");
        System.exit(-1);
    }

    public void cmdClearBoard(GtpCommand cmd) throws GtpError
    {
        initSize(m_size);
    }

    public void cmdEcho(GtpCommand cmd)
    {
        cmd.setResponse(cmd.getArgLine());
    }

    public void cmdEchoErr(GtpCommand cmd)
    {
        System.err.println(cmd.getArgLine());
    }

    public void cmdDelay(GtpCommand cmd) throws GtpError
    {
        cmd.checkNuArgLessEqual(1);
        if (cmd.getNuArg() == 1)
            m_delay = cmd.getIntArg(0, 0, Integer.MAX_VALUE);
        else
            cmd.getResponse().append(m_delay);
    }
    
    public void cmdEPList(GtpCommand cmd) throws GtpError
    {
        if (cmd.getNuArg() == 1 && cmd.getArg(0).equals("show"))
            cmd.setResponse(GoPoint.toString(m_ePList));
        else
            m_ePList = cmd.getPointListArg(m_size);
    }

    public void cmdGfx(GtpCommand cmd)
    {
        cmd.setResponse("LABEL A4 test\n" +
                        "COLOR green A5 A7 B9\n" +
                        "COLOR #980098 B7 B8\n" +
                        "SQUARE B5 C9\n" +
                        "MARK A6 B6\n" +
                        "TRIANGLE A9\n" +
                        "WHITE A1\n" +
                        "BLACK B1\n" +
                        "CIRCLE c8\n" +
                        "INFLUENCE a7 -1 b7 -0.75 c7 -0.5 d7 -0.25 e7 0"
                        + " f7 0.25 g7 0.5 h7 0.75 j7 1\n" +
                        "VAR b c1 w c2 b c3 b c4 w pass b c5\n" +
                        "TEXT Graphics Demo\n");
    }

    public void cmdGoGuiAnalyzeCommands(GtpCommand cmd) throws GtpError
    {
        cmd.checkArgNone();
        String response =
            "bwboard/Dummy BWBoard/dummy_bwboard\n" +
            "none/Dummy Crash/dummy_crash\n" +
            "none/Dummy Delay/dummy_delay %o\n" +
            "eplist/Dummy EPList/dummy_eplist\n" +
            "gfx/Dummy Gfx/dummy_gfx\n" +
            "none/Dummy Invalid/dummy_invalid\n" +
            "none/Dummy Live Gfx/dummy_live_gfx\n" +
            "string/Dummy Long Response/dummy_long_response %s\n" +
            "none/Dummy Next Failure/dummy_next_failure %s\n" +
            "none/Dummy Next Success/dummy_next_success %s\n" +
            "none/Dummy Sleep/dummy_sleep %s\n" +
            "none/Dummy Sleep 20s/dummy_sleep\n";
        cmd.setResponse(response);
    }

    public void cmdGenmove(GtpCommand cmd)
    {
        ++m_numberGenmove;
        if (m_numberGenmove == m_resign)
        {
            cmd.setResponse("resign");
            return;
        }
        int numberPossibleMoves = 0;
        for (int x = 0; x < m_size; ++x)
            for (int y = 0; y < m_size; ++y)
                if (! m_alreadyPlayed[x][y])
                    ++numberPossibleMoves;
        GoPoint point = null;
        if (numberPossibleMoves > 0)
        {
            int rand = m_random.nextInt(numberPossibleMoves);
            int index = 0;
            for (int x = 0; x < m_size && point == null; ++x)
                for (int y = 0; y < m_size && point == null; ++y)
                    if (! m_alreadyPlayed[x][y])
                    {
                        if (index == rand)
                            point = GoPoint.get(x, y);
                        ++index;
                    }
        }
        cmd.setResponse(GoPoint.toString(point));
        if (point != null)
            m_alreadyPlayed[point.getX()][point.getY()] = true;
    }

    public void cmdInterrupt(GtpCommand cmd) throws GtpError
    {
        cmd.checkArgNone();
    }

    public void cmdInvalid(GtpCommand cmd)
    {        
        printInvalidResponse("This is an invalid GTP response.\n" +
                             "It does not start with a status character.\n");
    }

    public void cmdLiveGfx(GtpCommand cmd) throws GtpError
    {
        cmd.checkArgNone();
        System.err.println("gogui-gfx: TEXT Live Graphics Demo");
        System.err.println("gogui-gfx: LABEL A4 test");
        sleep(1000);
        System.err.println("gogui-gfx: COLOR green A5 A7 B9");
        sleep(1000);
        System.err.println("gogui-gfx: COLOR #980098 B7 B8");
        sleep(1000);
        System.err.println("gogui-gfx:\n" +
                           "SQUARE B5 C9\n" +
                           "MARK A6 B6\n" +
                           "TRIANGLE A9\n");
        sleep(1000);
        System.err.println("gogui-gfx: WHITE A1");
        sleep(1000);
        System.err.println("gogui-gfx: BLACK B1");
        sleep(1000);
        System.err.println("gogui-gfx: CIRCLE c8");
        sleep(1000);
        System.err.println("gogui-gfx: INFLUENCE a7 -1 b7 -0.75 c7 "
                           + "-0.5 d7 -0.25 e7 0 f7 0.25 g7 0.5 h7 0.75 "
                           + "j7 1");
        sleep(1000);
        System.err.println("gogui-gfx: VAR b c1 w c2 b c3 b c4 w pass "
                           + "b c5");
        sleep(1000);
        System.err.println("gogui-gfx: CLEAR");
    }

    public void cmdLongResponse(GtpCommand cmd) throws GtpError
    {        
        cmd.checkNuArg(1);
        int n = cmd.getIntArg(0, 1, Integer.MAX_VALUE);
        for (int i = 1; i <= n; ++i)
        {
            cmd.getResponse().append(i);
            cmd.getResponse().append("\n");
        }
    }

    public void cmdNextFailure(GtpCommand cmd) throws GtpError
    {
        nextResponseFixed(cmd, false);
    }

    public void cmdNextSuccess(GtpCommand cmd) throws GtpError
    {
        nextResponseFixed(cmd, true);
    }

    public void cmdPlay(GtpCommand cmd) throws GtpError
    {
        cmd.checkNuArg(2);
        cmd.getColorArg(0);
        GoPoint point = cmd.getPointArg(1, m_size);
        if (point != null)
            m_alreadyPlayed[point.getX()][point.getY()] = true;
    }

    public void cmdSleep(GtpCommand cmd) throws GtpError
    {
        cmd.checkNuArgLessEqual(1);        
        long millis = 20000;
        if (cmd.getNuArg() == 1)
            millis = (long)(cmd.getDoubleArg(0) * 1000.0);
        sleep(millis);
    }

    public void handleCommand(GtpCommand cmd) throws GtpError
    {
        if (m_nextResponseFixed)
        {
            m_nextResponseFixed = false;
            if (! m_nextStatus)
                throw new GtpError(m_nextResponse);
            cmd.setResponse(m_nextResponse);
        }
        else
            super.handleCommand(cmd);
        if (m_delay > 0)
        {
            try
            {
                Thread.sleep(1000L * m_delay);
            }
            catch (InterruptedException e)
            {
            }
        }
    }

    public void interruptCommand()
    {
        m_thread.interrupt();
    }

    private boolean m_nextResponseFixed;

    private boolean m_nextStatus;

    /** Delay every command (seconds) */
    private int m_delay;

    private int m_numberGenmove;

    private int m_resign;

    private int m_size;

    private boolean[][] m_alreadyPlayed;

    private final Random m_random;

    private String m_nextResponse;

    private final Thread m_thread;

    /** Editable point list for dummy_eplist command. */
    private ArrayList m_ePList = new ArrayList();

    private void initSize(int size)
    {
        m_alreadyPlayed = new boolean[size][size];
        m_size = size;
        m_numberGenmove = 0;
    }

    private void nextResponseFixed(GtpCommand cmd, boolean nextStatus)
    {
        m_nextResponseFixed = true;
        m_nextStatus = nextStatus;
        m_nextResponse = cmd.getArgLine();
    }

    private void registerCommands()
    {
        register("boardsize", new GtpCallback() {
                public void run(GtpCommand cmd) throws GtpError {
                    cmdBoardsize(cmd); } });
        register("clear_board", new GtpCallback() {
                public void run(GtpCommand cmd) throws GtpError {
                    cmdClearBoard(cmd); } });
        register("dummy_bwboard", new GtpCallback() {
                public void run(GtpCommand cmd) throws GtpError {
                    cmdBWBoard(cmd); } });
        register("dummy_delay", new GtpCallback() {
                public void run(GtpCommand cmd) throws GtpError {
                    cmdDelay(cmd); } });
        register("dummy_eplist", new GtpCallback() {
                public void run(GtpCommand cmd) throws GtpError {
                    cmdEPList(cmd); } });
        register("dummy_gfx", new GtpCallback() {
                public void run(GtpCommand cmd) throws GtpError {
                    cmdGfx(cmd); } });
        register("dummy_invalid", new GtpCallback() {
                public void run(GtpCommand cmd) throws GtpError {
                    cmdInvalid(cmd); } });
        register("dummy_live_gfx", new GtpCallback() {
                public void run(GtpCommand cmd) throws GtpError {
                    cmdLiveGfx(cmd); } });
        register("dummy_long_response", new GtpCallback() {
                public void run(GtpCommand cmd) throws GtpError {
                    cmdLongResponse(cmd); } });
        register("dummy_crash", new GtpCallback() {
                public void run(GtpCommand cmd) throws GtpError {
                    cmdCrash(cmd); } });
        register("dummy_next_failure", new GtpCallback() {
                public void run(GtpCommand cmd) throws GtpError {
                    cmdNextFailure(cmd); } });
        register("dummy_next_success", new GtpCallback() {
                public void run(GtpCommand cmd) throws GtpError {
                    cmdNextSuccess(cmd); } });
        register("dummy_sleep", new GtpCallback() {
                public void run(GtpCommand cmd) throws GtpError {
                    cmdSleep(cmd); } });
        register("echo", new GtpCallback() {
                public void run(GtpCommand cmd) throws GtpError {
                    cmdEcho(cmd); } });
        register("echo_err", new GtpCallback() {
                public void run(GtpCommand cmd) throws GtpError {
                    cmdEchoErr(cmd); } });
        register("genmove", new GtpCallback() {
                public void run(GtpCommand cmd) throws GtpError {
                    cmdGenmove(cmd); } });
        register("gogui_analyze_commands", new GtpCallback() {
                public void run(GtpCommand cmd) throws GtpError {
                    cmdGoGuiAnalyzeCommands(cmd); } });
        register("gogui_interrupt", new GtpCallback() {
                public void run(GtpCommand cmd) throws GtpError {
                    cmdInterrupt(cmd); } });
        register("play", new GtpCallback() {
                public void run(GtpCommand cmd) throws GtpError {
                    cmdPlay(cmd); } });
    }

    private void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
        }
    }
}

//----------------------------------------------------------------------------
