package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.support.HaloConst;
import cc.ryanc.halo.model.support.Theme;
import cc.ryanc.halo.model.support.ThemeProperties;
import cc.ryanc.halo.service.ThemeService;
import cc.ryanc.halo.web.controller.content.base.BaseContentController;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : RYAN0UP
 * @date : 2019/3/26
 */
@Service
public class ThemeServiceImpl implements ThemeService {

    /**
     * Gets all themes
     *
     * @return list of themes
     */
    @Override
    public List<Theme> getThemes() {
        final List<Theme> themes = new ArrayList<>();
        final File[] files = getThemeBasePath().listFiles();
        try {
            if (null != files) {
                Theme theme;
                for (File file : files) {
                    if (!file.isDirectory()) {
                        continue;
                    }
                    theme = new Theme();
                    theme.setThemeDir(file.getName());
                    File optionsPath = new File(getThemeBasePath().getAbsolutePath(), file.getName() + "/module/options.ftl");
                    if (optionsPath.exists()) {
                        theme.setHasOptions(true);
                    } else {
                        theme.setHasOptions(false);
                    }
                    theme.setProperties(getProperties(new File(getThemeBasePath(), file.getName())));
                    themes.add(theme);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Themes scan failed", e);
        }
        return themes;
    }

    /**
     * Gets theme templates
     *
     * @param theme theme
     * @return List<String>
     */
    @Override
    public List<String> getTemplates(String theme) {
        final List<String> templates = new ArrayList<>();
        try {
            final File themesPath = new File(getThemeBasePath(), theme);
            final File modulePath = new File(themesPath.getAbsolutePath(), "module");
            final File[] baseFiles = themesPath.listFiles();
            final File[] moduleFiles = modulePath.listFiles();
            if (null != moduleFiles) {
                for (File file : moduleFiles) {
                    if (file.isFile() && file.getName().endsWith(HaloConst.SUFFIX_FTL)) {
                        templates.add("module/" + file.getName());
                    }
                }
            }
            if (null != baseFiles) {
                for (File file : baseFiles) {
                    if (file.isFile() && file.getName().endsWith(HaloConst.SUFFIX_FTL)) {
                        templates.add(file.getName());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get theme template", e);
        }
        return templates;
    }

    /**
     * Gets custom template, such as page_xxx.ftl, and xxx will be template name
     *
     * @param theme theme name
     * @return List
     */
    @Override
    public List<String> getCustomTpl(String theme) {
        final List<String> templates = new ArrayList<>();
        final File themePath = new File(getThemeBasePath(), theme);
        final File[] themeFiles = themePath.listFiles();
        if (null != themeFiles && themeFiles.length > 0) {
            for (File file : themeFiles) {
                String[] split = StrUtil.removeSuffix(file.getName(), HaloConst.SUFFIX_FTL).split("_");
                if (split.length == 2 && "page".equals(split[0])) {
                    templates.add(StrUtil.removeSuffix(file.getName(), HaloConst.SUFFIX_FTL));
                }
            }
        }
        return templates;
    }

    /**
     * Judging whether template exists under the specified theme
     *
     * @param template template
     * @return boolean
     */
    @Override
    public boolean isTemplateExist(String template) {
        StrBuilder templatePath = new StrBuilder(BaseContentController.THEME);
        templatePath.append("/");
        templatePath.append(template);
        File file = new File(getThemeBasePath(), templatePath.toString());
        return file.exists();
    }

    /**
     * Judging whether theme exists under template path
     *
     * @param theme theme name
     * @return boolean
     */
    @Override
    public boolean isThemeExist(String theme) {
        File file = new File(getThemeBasePath(), theme);
        return file.exists();
    }

    /**
     * Gets theme base path.
     *
     * @return File
     */
    @Override
    public File getThemeBasePath() {
        return new File(System.getProperties().getProperty("user.home"), ".halo/templates/themes");
    }

    /**
     * Get theme Properties.
     *
     * @param path path
     * @return ThemeProperties
     */
    @Override
    public ThemeProperties getProperties(File path) {
        File propertiesFile = new File(path, "theme.properties");
        ThemeProperties properties = new ThemeProperties();
        if (propertiesFile.exists()) {
            Props props = new Props(propertiesFile);
            properties.setId(props.getStr("theme.id"));
            properties.setName(props.getStr("theme.name"));
            properties.setWebsite(props.getStr("theme.website"));
            properties.setDescription(props.getStr("theme.description"));
            properties.setLogo(props.getStr("theme.logo"));
            properties.setVersion(props.getStr("theme.version"));
            properties.setAuthor(props.getStr("theme.author"));
            properties.setAuthorWebsite(props.getStr("theme.author.website"));
        }
        return properties;
    }
}