
    import re,os,glob
B='TMessagesProj/src/main/'
J=B+'java/org/telegram/'
rd=lambda p:open(p,encoding='utf8').read()
def wr(p,s): open(p,'w',encoding='utf8').write(s)
SC=r'<string name="OpenGramGlassScale"[^>]*>.*?</string>'
EN=[('OpenGramGlassSettings','Liquid Glass'),('OpenGramGlassOpacity','Glass opacity'),('OpenGramGlassScale','0 to 100'),('OpenGramGlassSettingsInfo','Glass transparency'),('OpenGramGlassPreview','Preview'),('OpenGramGlassSample','OpenGram'),('OpenGramGlassInfo','Saved automatically'),('OpenGramSettings','OpenGram'),('OpenGramSettingsInfo','Liquid Glass, verification badge'),('OpenGramVerifiedSettings','Verification badge'),('OpenGramVerifiedMajor','Major badge'),('OpenGramVerifiedInfo','Local badge, visible only to you.')]
RU=[('OpenGramSettings','OpenGram'),('OpenGramSettingsInfo','Liquid Glass, значок верификации'),('OpenGramVerifiedSettings','Значок верификации'),('OpenGramVerifiedMajor','Значок Мажор'),('OpenGramVerifiedInfo','Локальный значок, виден только вам.')]
def strings(p,items):
    s=re.sub(SC,'<string name="OpenGramGlassScale">0 to 100</string>',rd(p))
    add=''.join('    <string name="%s">%s</string>\n'%(k,v) for k,v in items if 'name="%s"'%k not in s)
    wr(p,s.replace('</resources>',add+'</resources>'))
for f in glob.glob(B+'res/values-*/strings.xml'):
    s=rd(f)
    if 'OpenGramGlassScale' in s: wr(f,re.sub(SC,'<string name="OpenGramGlassScale">0 to 100</string>',s))
strings(B+'res/values/strings.xml',EN)
strings(B+'res/values-ru/strings.xml',RU)
p=J+'ui/SettingsActivity.java'; s=rd(p)
s=s.replace('getString(R.string.OpenGramGlassSettings), getString(R.string.OpenGramGlassSettingsInfo)','getString(R.string.OpenGramSettings), getString(R.string.OpenGramSettingsInfo)')
s=s.replace('new OpenGramGlassSettingsActivity()','new OpenGramSettingsActivity()')
if 'OpenGramSettingsActivity' not in s:
    m=re.search(r'( *)items\.add\(SettingCell\.Factory\.of\(3,',s)
    s=s[:m.start()]+m.group(1)+'items.add(SettingCell.Factory.of(28, 0xFF62A8EA, 0xFF3978C8, R.drawable.settings_chat, getString(R.string.OpenGramSettings), getString(R.string.OpenGramSettingsInfo)));\n'+s[m.start():]
    m=re.search(r'( *)case 3:\n',s)
    s=s[:m.start()]+m.group(1)+'case 28:\n'+m.group(1)+'    presentSettingFragment(new OpenGramSettingsActivity());\n'+m.group(1)+'    break;\n'+s[m.start():]
wr(p,s)
K='opengram_major_badge'
def patch(p,old,new):
    s=rd(p)
    if K in s: return
    assert old in s,p
    wr(p,s.replace(old,new,1))
patch(J+'messenger/MessagesController.java','        TLRPC.User oldUser = users.get(user.id);\n        if (oldUser == user && !force)','        if (user.id == getUserConfig().getClientUserId() && getGlobalMainSettings().getBoolean("opengram_major_badge_" + currentAccount, false)) user.verified = true;\n        TLRPC.User oldUser = users.get(user.id);\n        if (oldUser == user && !force)')
patch(J+'messenger/UserConfig.java','            clientUserId = user.id;\n','            clientUserId = user.id;\n            if (MessagesController.getGlobalMainSettings().getBoolean("opengram_major_badge_" + currentAccount, false)) user.verified = true;\n')

aid=os.environ.get('TG_API_ID','').strip(); ah=os.environ.get('TG_API_HASH','').strip()
if aid.isdigit() and re.fullmatch(r'[0-9a-f]{32}',ah):
    p=J+'messenger/BuildVars.java'; s=rd(p)
    s=re.sub(r'public static int APP_ID = \d+;','public static int APP_ID = %s;'%aid,s)
    s=re.sub(r'public static String APP_HASH = "[0-9a-f]+";','public static String APP_HASH = "%s";'%ah,s)
    wr(p,s); print('API keys set')
else: print('WARNING: TG_API_ID/TG_API_HASH secrets missing, official keys left')
print('OK')