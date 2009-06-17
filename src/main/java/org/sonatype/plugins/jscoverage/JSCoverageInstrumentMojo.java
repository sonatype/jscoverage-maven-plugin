package org.sonatype.plugins.jscoverage;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @goal instrument
 * @phase pre-integration-test
 * @author velo
 */
public class JSCoverageInstrumentMojo
    extends AbstractJSCoverageInstrumentMojo
{

    /**
     * Javascript root source directory to be instrumented
     *
     * @parameter expression="${jscoverage.source}"
     * @required
     */
    private File source;

    /**
     * Javascript instrumented files destination
     *
     * @parameter expression="${jscoverage.destination}"
     */
    private File destination;

    /**
     * Similar to doNotInstrument configuration. doNotInstrument just doesn't instrument the code, exclude option won't
     * include the excluded files at destination directory
     *
     * @parameter
     */
    private String[] exclude;

    protected void validate()
        throws MojoFailureException, MojoExecutionException
    {
        super.validate();

        if ( source == null || !source.exists() )
        {
            throw new MojoFailureException( "Source folder does not exists! " + source );
        }

        if ( destination == null )
        {
            destination = source;

            source = new File( destination.getParentFile(), destination.getName() + ".original" );
            source.mkdirs();

            try
            {
                // backup the original files
                FileUtils.copyDirectoryStructure( destination, source );

                // keep destination empty
                FileUtils.forceDelete( destination );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
        }
    }

    protected void customizeCommandLine( Commandline cmd )
    {
        super.customizeCommandLine( cmd );

        if ( exclude != null )
        {
            for ( String noInstrument : exclude )
            {
                cmd.createArg().setValue( "--exclude=" + noInstrument );
            }
        }

        cmd.createArg().setValue( source.getAbsolutePath() );
        cmd.createArg().setValue( destination.getAbsolutePath() );
    }

    protected String getExecutable()
    {
        return "jscoverage";
    }

}
