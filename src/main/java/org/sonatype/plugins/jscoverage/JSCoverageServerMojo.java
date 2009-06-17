package org.sonatype.plugins.jscoverage;

import java.io.File;

import org.codehaus.plexus.util.cli.Commandline;

/**
 * @goal start-jscoverage
 * @phase pre-integration-test
 */
public class JSCoverageServerMojo
    extends AbstractJSCoverageInstrumentMojo
{

    /**
     * Run as a proxy server.
     *
     * @parameter expression="${jscoverage.proxy}"
     */
    private boolean runAsProxy;

    /**
     * Serve web content from the directory given by PATH. This option may not be given with the --proxy option.
     *
     * @parameter expression="${jscoverage.document-root}"
     */
    private File webappRoot;

    /**
     * Run the server on the port given by PORT.
     *
     * @parameter default-value="8080" expression="${jscoverage.port}"
     */
    private int port;

    /**
     * Use the directory given by PATH for storing coverage reports.
     *
     * @parameter default-value="${project.build.directory}/jscoverage-report" expression="${jscoverage.report-dir}"
     */
    private File reportDir;

    protected void customizeCommandLine( Commandline cmd )
    {
        super.customizeCommandLine( cmd );

        cmd.createArg().setValue( "--port=" + port );
        cmd.createArg().setValue( "--report-dir=" + reportDir.getAbsolutePath() );

        if ( runAsProxy )
        {
            cmd.createArg().setValue( "--proxy" );
        }
        else
        {
            if ( webappRoot != null )
            {
                cmd.createArg().setValue( "--document-root=" + webappRoot.getAbsolutePath() );
            }
        }
    }

    protected String getExecutable()
    {
        return "jscoverage-server";
    }

}
