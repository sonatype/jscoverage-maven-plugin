package org.sonatype.plugins.jscoverage;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @goal start-jscoverage
 * @phase pre-integration-test
 */
public class JSCoverageServerMojo
    extends AbstractMojo
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
     * Assume that all JavaScript files use the given character encoding. The default is ISO-8859-1. Note that if you
     * use the --proxy option, the character encoding will be determined from the charset parameter in the Content-Type
     * HTTP header.
     *
     * @parameter expression="${jscoverage.encoding}"
     */
    private String encoding;

    /**
     * Use the specified JavaScript version; valid values for VERSION are 1.0, 1.1, 1.2, ..., 1.8, or ECMAv3
     *
     * @parameter expression="${jscoverage.js-version}"
     */
    private String javascriptVersion;

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

    /**
     * @parameter default-value="${project.build.directory}"
     * @readonly
     */
    private File target;

    /**
     * Do not instrument JavaScript code from URL.
     *
     * @parameter
     */
    private String[] excludeCoverage;

    public void execute()
        throws MojoExecutionException
    {
        Commandline cmd = new Commandline();
        cmd.setExecutable( "jscoverage-server" );

        cmd.setWorkingDirectory( target );

        cmd.createArg().setValue( "--verbose" );
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
            if ( encoding != null )
            {
                cmd.createArg().setValue( "--encoding==" + encoding );
            }
        }

        if ( javascriptVersion != null )
        {
            cmd.createArg().setValue( "--js-version=" + javascriptVersion );
        }

        if ( excludeCoverage != null )
        {
            for ( String noInstrument : excludeCoverage )
            {
                cmd.createArg().setValue( "--no-instrument=" + noInstrument );
            }
        }

        StreamConsumer logger = new StreamConsumer()
        {
            public void consumeLine( String line )
            {
                getLog().debug( line );
            }
        };

        try
        {
            CommandLineUtils.executeCommandLine( cmd, logger, logger );
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }
}
