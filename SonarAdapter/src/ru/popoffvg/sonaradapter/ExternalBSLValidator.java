package ru.popoffvg.sonaradapter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.HistogramDiff;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.util.CancelIndicator;

import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.validation.CustomValidationMessageAcceptor;
import com._1c.g5.v8.dt.bsl.validation.IExternalBslValidator;
import com._1c.g5.v8.dt.core.platform.IV8ProjectManager;
import com.google.inject.Inject;

import ru.popoffvg.sonaradapter.SonarValidatorHelper.TextRangeInfo;
import ru.popoffvg.sonarqube.Issue;
import ru.popoffvg.sonarqube.Issue.SonarTextRange;
import ru.popoffvg.sonarqube.SonarQube;

public class ExternalBSLValidator implements IExternalBslValidator {

	private static String QUIQK_FIX_CODE = "SONAR_QUICK_FIX";//$NON-NLS-1$
	private EObject moduleObject;
	private CustomValidationMessageAcceptor acceptor;
	private String moduleText;
	EditList diff;
	Optional<String> sonarText;
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
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		String path = root.getFile(new Path(EcoreUtil.getURI(moduleObject).toPlatformString(true)))
				.getProjectRelativePath()
				.toString();
		
		String componentKey = projectId + ":" + path;
		SonarQube sonar = SonarQube.create(server, prefs.getString(SonarAdapterPlugin.KEY_ID_PROPERTY));
		
		// TODO Добавить кеширование
		sonarText = sonar.getRawText(componentKey);
		SonarValidatorHelper helper = SonarValidatorHelper.create(moduleText, sonarText.orElse(moduleText));
		
		HashMap<String, String> params = new HashMap<>();
		params.put("componentKeys", componentKey);
		params.put("resolved", "false");
		if (!assign.isEmpty()) params.put("assignees", assign);
		sonar.search(params)
			.filter(issue -> excludePattern == null || !excludePattern.matcher(issue.getMessage()).find())
			.forEach(issue -> regIssueFromSonar(issue, helper));
	}

	public void regIssueFromSonar(Issue issue, SonarValidatorHelper helper) {
		SonarTextRange textRange = issue.getTextRange();
		int length = textRange.getEndOffset() - textRange.getStartOffset();
		TextRangeInfo info = helper.info(textRange.getStartLine(), textRange.getStartOffset(), length);
		// TODO замечания на несколько строк?
		if(info.isRemoved()) {
			acceptor.acceptInfo("sonar: " + issue.getMessage(),
					moduleObject,
					info.getStartOffset(),
					length,
					QUIQK_FIX_CODE);
		}
	}

}
