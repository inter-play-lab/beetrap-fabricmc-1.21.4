# GitHub Pages Configuration Guide

This guide will walk you through configuring the GitHub Pages site for the Beetrap project.

## Step 1: Update Configuration Files

### 1.1 Update _config.yml

Edit the `docs/_config.yml` file and replace the placeholder values:

```yaml
# Replace these placeholder values:
url: "https://your-username.github.io"          # Replace with your GitHub username
baseurl: "/beetrap-fabricmc-1.21.4"             # Update if your repo name is different
author: "Beetrap Project Team"                   # Update with your name/team
email: "your-email@example.com"                 # Replace with your email
github_username: "your-username"                # Replace with your GitHub username
```

**Example configuration:**
```yaml
url: "https://johndoe.github.io"
baseurl: "/beetrap-fabricmc-1.21.4"
author: "John Doe"
email: "john.doe@example.com"
github_username: "johndoe"
```

### 1.2 Update index.html (Optional)

If you want to customize the main page, edit `docs/index.html`:

- Update the GitHub link in the "Get Started" section
- Modify project description if needed
- Add your contact information

Look for this line and update the URL:
```html
<a href="https://github.com/your-username/beetrap-fabricmc-1.21.4" class="btn">View on GitHub</a>
```

## Step 2: Enable GitHub Pages

### 2.1 Push Your Changes

First, commit and push your configuration changes:

```bash
git add docs/
git commit -m "Configure GitHub Pages"
git push origin main
```

### 2.2 Enable GitHub Pages on GitHub

1. Go to your repository on GitHub
2. Click on **Settings** tab
3. Scroll down to **Pages** section in the left sidebar
4. Under **Source**, select "Deploy from a branch"
5. Choose **main** branch
6. Select **/ docs** folder
7. Click **Save**

### 2.3 Wait for Deployment

- GitHub will automatically build and deploy your site
- This usually takes 1-5 minutes
- You'll see a green checkmark when it's ready
- Your site will be available at: `https://your-username.github.io/beetrap-fabricmc-1.21.4/`

## Step 3: Verify Your Site

1. Visit your GitHub Pages URL
2. Check that all links work properly
3. Verify the content displays correctly
4. Test navigation between pages

## Step 4: Custom Domain (Optional)

If you want to use a custom domain:

1. Add a `CNAME` file to the `docs/` directory
2. Put your domain name in the file (e.g., `beetrap.yourdomain.com`)
3. Configure your DNS settings to point to GitHub Pages
4. Update the `url` in `_config.yml` to your custom domain

## Troubleshooting

### Common Issues:

1. **Site not loading**: Check that GitHub Pages is enabled and the source is set correctly
2. **404 errors**: Verify the `baseurl` in `_config.yml` matches your repository name
3. **Styling issues**: Clear your browser cache and check for CSS errors
4. **Build failures**: Check the Actions tab in your GitHub repository for error messages

### Build Status:

You can check the build status in your repository:
- Go to **Actions** tab
- Look for "pages build and deployment" workflows
- Click on failed builds to see error details

## Local Development

To test changes locally before pushing:

```bash
# Install Jekyll (one time setup)
gem install jekyll bundler

# Navigate to docs directory
cd docs

# Serve the site locally
jekyll serve

# Open http://localhost:4000 in your browser
```

## Maintenance

### Regular Updates:

1. Keep content up to date
2. Update links if repository structure changes
3. Monitor for broken links
4. Update dependencies if using custom gems

### Adding New Pages:

1. Create new HTML or Markdown files in the `docs/` directory
2. Add navigation links in existing pages
3. Update `_config.yml` if needed for navigation

## Support

If you encounter issues:

1. Check GitHub Pages documentation
2. Review Jekyll documentation
3. Check the repository's Issues section
4. Verify your configuration matches the examples above

Your GitHub Pages site should now be properly configured and accessible!