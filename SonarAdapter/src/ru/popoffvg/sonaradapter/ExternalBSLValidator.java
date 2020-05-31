package ru.popoffvg.sonaradapter;

import java.util.HashMap;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.util.CancelIndicator;

import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.validation.CustomValidationMessageAcceptor;
import com._1c.g5.v8.dt.bsl.validation.IExternalBslValidator;
import com._1c.g5.v8.dt.core.platform.IV8ProjectManager;
import com.google.inject.Inject;

import ru.popoffvg.sonarqube.Issue;
import ru.popoffvg.sonarqube.Issue.SonarTextRange;
import ru.popoffvg.sonarqube.SonarQube;

public class ExternalBSLValidator implements IExternalBslValidator {

	private static String quickFixCode = "SONAR_QUICK_FIX";//$NON-NLS-1$
	private EObject moduleObject;
	private CustomValidationMessageAcceptor acceptor;
	private String moduleText;
	@Inject
	private IV8ProjectManager projectManager;
	private Pattern excludePattern;
	@Inject
	private SonarAdapterPlugin sonarAdapter;

	public ExternalBSLValidator() {
		super();
	}
	
	@Override
	public boolean needValidation(EObject object) {
		return object instanceof Module;
	}

	@Override
	public void validate(EObject object, CustomValidationMessageAcceptor messageAcceptor, CancelIndicator monitor) {

		IProject project = projectManager.getProject(object).getProject();
		IPreferenceStore prefs = sonarAdapter.getPreferenceStore(project);
		if(!prefs.getBoolean(SonarAdapterPlugin.LOAD_SONAR_ISSUES_PROPERTY)) return;
		
		String projectId = prefs.getString(SonarAdapterPlugin.PROJECT_ID_PROPERTY);
		String server = prefs.getString(SonarAdapterPlugin.SERVER_ID_PROPERTY);
		String assign = prefs.getString(SonarAdapterPlugin.ASSIGNED_PROPERTY);
		
		if(!prefs.getString(SonarAdapterPlugin.EXCLUDES_ID_PROPERTY).isEmpty())
			excludePattern = Pattern.compile(prefs.getString(SonarAdapterPlugin.EXCLUDES_ID_PROPERTY));
		
		if(projectId.isEmpty() || server.isEmpty()) {
			SonarAdapterPlugin.log(
					SonarAdapterPlugin.createWarningStatus("Missing Sonar check. Server parameters are empty."));
			return;
		}
		
		this.moduleObject = object;
		this.acceptor = messageAcceptor;
		moduleText = NodeModelUtils.findActualNodeFor(object).getText();

		String path = ResourcesPlugin.getWorkspace().getRoot()
					.getFile(new Path(EcoreUtil.getURI(moduleObject).toPlatformString(true)))
					.getProjectRelativePath().toString();
		
		HashMap<String, String> params = new HashMap<>();
		params.put("componentKeys", projectId + ":" + path);
		params.put("resolved", "false");
		if (!assign.isEmpty())
			params.put("assignees", assign);

		SonarQube sonar = SonarQube.create(server, prefs.getString(SonarAdapterPlugin.KEY_ID_PROPERTY));
		sonar.search(params)
			.filter(issue -> excludePattern == null || !excludePattern.matcher(issue.getMessage()).find())
			.forEach(this::regIssueFromSonar);
		
	}

	public void regIssueFromSonar(Issue issue) {
		SonarTextRange textRange = issue.getTextRange();
		int startOffset = startOffsetFromTextRange(textRange.getStartLine(), textRange.getStartOffset());
		acceptor.acceptInfo("sonar: " + issue.getMessage(),
				moduleObject,
				startOffset,
				textRange.getEndOffset() - textRange.getStartOffset(),
				quickFixCode);
	}

	private int startOffsetFromTextRange(int startLine, int startOffset) {
		return calculateLineOffset(moduleText, startLine)+startOffset;
	}

	private int calculateLineOffset(String text, int startLine) {
		return text.lines()
				.limit(startLine - 1)
				.mapToInt(String::length)
				.map(l -> l + 1)
				.sum();
	}
}
