package org.sonatype.plugins.jscoverage;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

public abstract class AbstractJSCoverageMojo
    extends AbstractMojo
{

    /**
     * @parameter default-value="${project.build.directory}"
     * @readonly
     */
    private File target;

    /**
     * @parameter default-value="false" expression="${skip.jscoverage}"
     */
    private boolean skipCoverage;

    public AbstractJSCoverageMojo()
    {
        super();
    }

    public final void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( skipCoverage )
        {
            getLog().warn( "Skipping coverage" );
            return;
        }

        validate();

        Commandline cmd = new Commandline();
        cmd.setExecutable( getExecutable() );

        cmd.setWorkingDirectory( target );

        customizeCommandLine( cmd );

        StreamConsumer logger = new StreamConsumer()
        {
            public void consumeLine( String line )
            {
                getLog().info( line );
            }
        };

        try
        {
            int code = CommandLineUtils.executeCommandLine( cmd, logger, logger );

            if ( code != 0 )
            {
                throw new MojoFailureException( "Failed to invoke jscoverage, see log for details" );
            }
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }

    protected abstract void customizeCommandLine( Commandline cmd );

    protected void validate()
        throws MojoFailureException, MojoExecutionException
    {
        // to be overwritten
    }

    protected abstract String getExecutable();

}