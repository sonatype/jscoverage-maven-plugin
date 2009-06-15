package org.sonatype.plugins.jscoverage;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @goal stop-jscoverage
 * @phase post-integration-test
 */
public class JSCoverageServerShutdownMojo
    extends AbstractMojo
{

    /**
     * @parameter default-value="${project.build.directory}"
     * @readonly
     */
    private File target;

    public void execute()
        throws MojoExecutionException
    {
        Commandline cmd = new Commandline();
        cmd.setExecutable( "jscoverage-server" );

        cmd.setWorkingDirectory( target );

        cmd.createArg().setValue( "--shutdown" );

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
