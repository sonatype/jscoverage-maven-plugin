package org.sonatype.plugins.jscoverage;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

/**
 * @goal report
 * @phase post-integration-test
 * @author velo
 */
public class ReportMojo
    extends AbstractMojo
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
     * @parameter default-value="${basedir}/jscoverage.json.result"
     * @required
     */
    private File persistedResults;

    /**
     * @parameter default-value="${project.reporting.outputDirectory}/jscoverage"
     */
    private File reportOutput;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( destination == null )
        {
            destination = source;
        }

        try
        {
            FileUtils.copyDirectoryStructure( destination, reportOutput );
            FileUtils.copyFile( persistedResults, new File( reportOutput, "jscoverage.json" ) );
            copyUrl( "jscoverage-highlight.css" );
            copyUrl( "jscoverage-ie.css" );
            copyUrl( "jscoverage-throbber.gif" );
            copyUrl( "jscoverage.css" );
            copyUrl( "jscoverage.html" );
            copyUrl( "jscoverage.js" );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }

    private void copyUrl( String url )
        throws IOException
    {
        FileUtils.copyURLToFile( getClass().getResource( "/" + url ), new File( reportOutput, url ) );
    }

}
