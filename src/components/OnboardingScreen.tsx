import React, { useState, useEffect } from "react";

interface OnboardingScreenProps {
  onSignIn: (user: { name: string; email: string; avatar: string }) => void;
}

export default function OnboardingScreen({ onSignIn }: OnboardingScreenProps) {
  const [taglineIndex, setTaglineIndex] = useState(0);
  const [showAccountSelector, setShowAccountSelector] = useState(false);
  const taglines = ["Write your mind.", "Speak your day.", "Scan your journal."];

  useEffect(() => {
    const interval = setInterval(() => {
      setTaglineIndex((prev) => (prev + 1) % taglines.length);
    }, 3200);
    return () => clearInterval(interval);
  }, []);

  const handleAccountSelect = (name: string, email: string, avatar: string) => {
    setShowAccountSelector(false);
    onSignIn({ name, email, avatar });
  };

  return (
    <div className="flex-1 flex flex-col justify-between px-6 pb-8 pt-4 paper-texture overflow-y-auto relative">
      
      {/* Background soft lighting glows */}
      <div className="absolute inset-0 pointer-events-none overflow-hidden z-0">
        <div className="absolute -top-[10%] -left-[10%] w-[50%] h-[50%] rounded-full bg-slate-300/20 blur-[80px]"></div>
        <div className="absolute -bottom-[10%] -right-[10%] w-[50%] h-[50%] rounded-full bg-slate-400/10 blur-[80px]"></div>
      </div>

      <div className="relative z-10 flex flex-col items-center flex-1 justify-center py-6">
        
        {/* Branding Section with Vibrant Palette signature styles */}
        <div className="mb-10 flex flex-col items-center">
          <div className="w-16 h-16 bg-gradient-to-br from-indigo-500 via-purple-500 to-rose-500 rounded-[1.5rem] flex items-center justify-center mb-6 shadow-lg shadow-indigo-100 transform transition-transform hover:scale-105 duration-500">
            <span className="material-symbols-outlined text-white !text-[36px]" style={{ fontVariationSettings: "'FILL' 1" }}>
              auto_stories
            </span>
          </div>
          
          <h1 className="font-sans text-4xl font-black text-slate-900 tracking-tight leading-tight">
            SoulJournal<span className="text-rose-500">.</span>
          </h1>
          
          <div className="h-8 flex items-center justify-center overflow-hidden mt-1">
            <p className="font-sans text-xs text-slate-400 font-bold tracking-widest uppercase transition-all duration-700">
              {taglines[taglineIndex]}
            </p>
          </div>
        </div>

        {/* Sophisticated Tabletop Journal Illustration */}
        <div className="w-full mb-10 px-2">
          <div className="aspect-[16/10] w-full rounded-2xl overflow-hidden shadow-xl bg-journal-card-low relative group border border-slate-200/50">
            <img 
              className="w-full h-full object-cover transition-transform duration-[10000ms] ease-out group-hover:scale-110" 
              src="https://lh3.googleusercontent.com/aida-public/AB6AXuD1CsSZm7r6iRef0gbQlJetD4zf-dY4dC_Znklv_ysIEux-tsUljg_2ilKrf8N3P3786CaUbnu0azf-sy6m8obfQkuG-_H0_rVe-f7WeQpwb_qXOFDV1hhWPaGz2Nc-oC9tF-hL0kjB3juTHpzR6qu8xP4JgCsOmZs74AmNNNHqjVRPJjvq2yQl66j4ZKT0Xw4SonZgHbk0L72lok6u8Jns7g-0FauTRXtYepQxz3TB1Q5PbVSYNjb1tWAE1czXD5w8Z2UXlEZb2aJU" 
              alt="Tabletop with journal and fountain pen"
              referrerPolicy="no-referrer"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-indigo-900/30 to-transparent"></div>
          </div>
        </div>

        {/* Authentication Options */}
        <div className="w-full max-w-sm flex flex-col gap-4">
          <button 
            id="google-sign-in"
            onClick={() => setShowAccountSelector(true)}
            className="flex items-center justify-center gap-4 bg-white border border-slate-200 py-3.5 px-6 rounded-full w-full shadow-lg shadow-slate-100 hover:shadow-xl hover:shadow-indigo-50/50 transition-all duration-300 hover:bg-slate-50 hover:border-rose-300 active:scale-[0.98] cursor-pointer"
          >
            <svg className="w-5 h-5" height="20" viewBox="0 0 20 20" width="20" xmlns="http://www.w3.org/2000/svg">
              <path d="M19.6 10.227c0-.709-.064-1.39-.182-2.045H10v3.873h5.382a4.6 4.6 0 0 1-1.996 3.014v2.504h3.232c1.89-1.738 2.982-4.304 2.982-7.346z" fill="#4285F4"></path>
              <path d="M10 20c2.7 0 4.964-.895 6.618-2.427l-3.232-2.504c-.895.6-2.04.954-3.386.954-2.605 0-4.81-1.76-5.595-4.123H1.423v2.582A9.996 9.996 0 0 0 10 20z" fill="#34A853"></path>
              <path d="M4.405 11.9c-.2-.6-.314-1.24-.314-1.9s.114-1.3.314-1.9V5.518H1.423a9.996 9.996 0 0 0 0 8.964l2.982-2.582z" fill="#FBBC05"></path>
              <path d="M10 3.968c1.47 0 2.786.505 3.823 1.495l2.868-2.868C14.96 1.105 12.695 0 10 0 6.11 0 2.75 2.214 1.423 5.518l2.982 2.582C5.19 5.727 7.395 3.968 10 3.968z" fill="#EA4335"></path>
            </svg>
            <span className="font-sans font-extrabold text-[15px] text-slate-800">Sign in with Google</span>
          </button>
          
          <div className="px-4">
            <p className="font-sans text-xs text-slate-400 font-medium leading-relaxed text-center">
              Your data is securely locked to your Google ID and stored in your personal cloud repository.
            </p>
          </div>
        </div>

      </div>

      {/* Footer / Version */}
      <div className="text-center opacity-40 font-mono text-[10px] tracking-widest text-journal-text-muted relative z-10">
        SOULJOURNAL V1.0.4
      </div>

      {/* Google Account Selector Dialog Mock */}
      {showAccountSelector && (
        <div className="absolute inset-0 bg-black/40 backdrop-blur-sm z-50 flex items-end justify-center">
          <div className="w-full bg-white rounded-t-[28px] p-6 shadow-2xl space-y-6 max-h-[80%] overflow-y-auto animate-slide-up">
            
            <div className="flex flex-col items-center text-center">
              <svg className="w-8 h-8 mb-2" height="32" viewBox="0 0 20 20" width="32" xmlns="http://www.w3.org/2000/svg">
                <path d="M19.6 10.227c0-.709-.064-1.39-.182-2.045H10v3.873h5.382a4.6 4.6 0 0 1-1.996 3.014v2.504h3.232c1.89-1.738 2.982-4.304 2.982-7.346z" fill="#4285F4"></path>
                <path d="M10 20c2.7 0 4.964-.895 6.618-2.427l-3.232-2.504c-.895.6-2.04.954-3.386.954-2.605 0-4.81-1.76-5.595-4.123H1.423v2.582A9.996 9.996 0 0 0 10 20z" fill="#34A853"></path>
                <path d="M4.405 11.9c-.2-.6-.314-1.24-.314-1.9s.114-1.3.314-1.9V5.518H1.423a9.996 9.996 0 0 0 0 8.964l2.982-2.582z" fill="#FBBC05"></path>
                <path d="M10 3.968c1.47 0 2.786.505 3.823 1.495l2.868-2.868C14.96 1.105 12.695 0 10 0 6.11 0 2.75 2.214 1.423 5.518l2.982 2.582C5.19 5.727 7.395 3.968 10 3.968z" fill="#EA4335"></path>
              </svg>
              <h3 className="font-sans font-semibold text-lg text-journal-text-dark">Sign in with Google</h3>
              <p className="text-xs text-journal-text-muted mt-1">to continue to Ethereal Journal</p>
            </div>

            <div className="space-y-3">
              {/* Account 1: Chinmay (Female portrait from mock screen) */}
              <button 
                id="account-chinmay"
                onClick={() => handleAccountSelect(
                  "Chinmay", 
                  "chinmayrk01@gmail.com", 
                  "https://lh3.googleusercontent.com/aida-public/AB6AXuC5m7o5vlfMJLuMytcnNNaGaLXPIIOiamQWrMkgR1UpGXAAAzLa1ycClqmYu78TQVbt0fd22JZ_z67sToAU3YcVjwLcDAP80te3Bavbuh_UGzPHK0jcRx8I06lvfu7_6pJG27ZT3EB10JXu61rhK5tFYoImKj37jzzTTxYQCXbh5uK1q8vbo_2aewsdGt8A39k6doLBRw2X_5IwsT2TyuzpnkItgi4VLngYOv3uK4-x-acJhtuYSygLEzMCMOQkkFulgs9K-C-TuFxG"
                )}
                className="w-full flex items-center gap-3 p-3 rounded-2xl border border-slate-200 hover:bg-journal-card-low transition-colors text-left"
              >
                <img 
                  className="w-10 h-10 rounded-full object-cover border border-slate-200" 
                  src="https://lh3.googleusercontent.com/aida-public/AB6AXuC5m7o5vlfMJLuMytcnNNaGaLXPIIOiamQWrMkgR1UpGXAAAzLa1ycClqmYu78TQVbt0fd22JZ_z67sToAU3YcVjwLcDAP80te3Bavbuh_UGzPHK0jcRx8I06lvfu7_6pJG27ZT3EB10JXu61rhK5tFYoImKj37jzzTTxYQCXbh5uK1q8vbo_2aewsdGt8A39k6doLBRw2X_5IwsT2TyuzpnkItgi4VLngYOv3uK4-x-acJhtuYSygLEzMCMOQkkFulgs9K-C-TuFxG"
                  alt="Chinmay"
                  referrerPolicy="no-referrer"
                />
                <div>
                  <p className="font-semibold text-sm text-journal-text-dark">Chinmay</p>
                  <p className="text-xs text-journal-text-muted">chinmayrk01@gmail.com</p>
                </div>
              </button>

              {/* Account 2: Mindful Journaler */}
              <button 
                id="account-guest"
                onClick={() => handleAccountSelect(
                  "Mindful Journaler", 
                  "guest@ethereal.journal", 
                  "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=150&h=150"
                )}
                className="w-full flex items-center gap-3 p-3 rounded-2xl border border-slate-200 hover:bg-journal-card-low transition-colors text-left"
              >
                <div className="w-10 h-10 rounded-full bg-journal-teal/20 flex items-center justify-center text-journal-primary font-bold">
                  MJ
                </div>
                <div>
                  <p className="font-semibold text-sm text-journal-text-dark">Mindful Journaler</p>
                  <p className="text-xs text-journal-text-muted">guest@ethereal.journal</p>
                </div>
              </button>
            </div>

            <button 
              onClick={() => setShowAccountSelector(false)}
              className="w-full py-3 text-center text-sm font-semibold text-journal-text-muted hover:text-journal-primary transition-colors"
            >
              Cancel
            </button>

          </div>
        </div>
      )}
    </div>
  );
}
