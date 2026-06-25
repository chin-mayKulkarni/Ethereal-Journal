import React, { useState, useEffect, useRef } from "react";
import { DiaryEntry, AISummary } from "../types";

interface DashboardScreenProps {
  user: { name: string; email: string; avatar: string };
  onSignOut: () => void;
  entries: DiaryEntry[];
  onAddEntry: (text: string, mood: string) => void;
  onDeleteEntry: (id: string) => void;
  summary: AISummary | null;
  summaryLoading: boolean;
  onFetchSummary: (refinementText?: string) => void;
}

export default function DashboardScreen({
  user,
  onSignOut,
  entries,
  onAddEntry,
  onDeleteEntry,
  summary,
  summaryLoading,
  onFetchSummary,
}: DashboardScreenProps) {
  // Navigation State
  const [activeTab, setActiveTab] = useState<"journal" | "stats" | "summary">("journal");

  // Journal Entry State
  const [newText, setNewText] = useState("");
  const [selectedMood, setSelectedMood] = useState("Focused");
  const [isVoiceActive, setIsVoiceActive] = useState(false);
  const [isRecording, setIsRecording] = useState(false);
  const [voiceTimer, setVoiceTimer] = useState(0);
  const [showSettings, setShowSettings] = useState(false);

  // Stats Interactive State
  const [simulatedSteps, setSimulatedSteps] = useState(12402);
  const [simulatedSleep, setSimulatedSleep] = useState({ hours: 7, minutes: 20 });
  const [energyPercent, setEnergyPercent] = useState(84);
  const [copied, setCopied] = useState(false);
  const [showShareModal, setShowShareModal] = useState(false);

  // Summary Refinement Chat State
  const [refinementInput, setRefinementInput] = useState("");
  const [activeRefinements, setActiveRefinements] = useState<string[]>([]);

  const voiceIntervalRef = useRef<NodeJS.Timeout | null>(null);

  const moods = [
    { name: "Focused", icon: "center_focus_strong", bg: "bg-blue-50 text-blue-700 border-blue-200" },
    { name: "Reflective", icon: "psychology", bg: "bg-indigo-50 text-indigo-700 border-indigo-200" },
    { name: "Anxious", icon: "warning", bg: "bg-amber-50 text-amber-700 border-amber-200" },
    { name: "Grateful", icon: "favorite", bg: "bg-rose-50 text-rose-700 border-rose-200" },
    { name: "Neutral", icon: "sentiment_satisfied", bg: "bg-slate-50 text-slate-700 border-slate-200" },
  ];

  // Voice recording simulation
  useEffect(() => {
    if (isRecording) {
      voiceIntervalRef.current = setInterval(() => {
        setVoiceTimer((t) => t + 1);
      }, 1000);
    } else {
      if (voiceIntervalRef.current) clearInterval(voiceIntervalRef.current);
      setVoiceTimer(0);
    }
    return () => {
      if (voiceIntervalRef.current) clearInterval(voiceIntervalRef.current);
    };
  }, [isRecording]);

  const handleSaveEntry = () => {
    if (!newText.trim()) return;
    onAddEntry(newText, selectedMood);
    setNewText("");
    setIsVoiceActive(false);

    // Dynamic stat modification on logs saved
    setEnergyPercent((prev) => Math.min(100, prev + 4));
    setSimulatedSteps((prev) => prev + 120);
  };

  const startVoiceRecording = () => {
    setIsRecording(true);
  };

  const stopVoiceRecording = () => {
    setIsRecording(false);
    const voiceTranscripts: Record<string, string> = {
      Focused: "Thinking about the architecture proposal. I need to make sure the server-side API routes are perfectly isolated and secure, keeping all keys hidden from client exposure. Today's sessions went exceptionally well.",
      Reflective: "Reflecting on the recent group meeting. Navigated several highly complex interpersonal loops with key project stake-holders. Dialogue felt open and deeply aligned.",
      Anxious: "A bit worried about the timing of tomorrow's release. I should probably review the rendering layout in the morning to make sure there are no clipping anomalies.",
      Grateful: "Extremely appreciative of the quick response from chinmayrk01. The collaborative feedback loop was outstandingly warm and professional.",
      Neutral: "Just registering a standard mid-day alignment. All indicators are green and operational.",
    };
    setNewText(voiceTranscripts[selectedMood] || "Spoken reflection compiled successfully.");
  };

  const formattedVoiceTimer = () => {
    const m = Math.floor(voiceTimer / 60).toString().padStart(2, "0");
    const s = (voiceTimer % 60).toString().padStart(2, "0");
    return `${m}:${s}`;
  };

  // Word count helper
  const getTotalLoggedWords = () => {
    return entries.reduce((acc, entry) => {
      const words = entry.text.trim().split(/\s+/).filter(Boolean).length;
      return acc + words;
    }, 0);
  };

  // Copy Summary Handler
  const handleCopySummary = () => {
    if (!summary) return;
    const textToCopy = `AI Summary - June 25, 2026\n\nMilestones:\n${summary.milestones.map(m => `- ${m}`).join("\n")}\n\nActionable Vectors:\n${summary.actionableVectors.map(v => `- ${v}`).join("\n")}\n\nEmotional Tone:\n${summary.emotionalTone.map(t => `- ${t}`).join("\n")}`;
    navigator.clipboard.writeText(textToCopy);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  // Trigger AI Summary from button & go to tab
  const handleTriggerAISummary = () => {
    setActiveTab("summary");
    if (!summary) {
      onFetchSummary();
    }
  };

  // Chat Refinement Submission
  const handleRefineSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!refinementInput.trim()) return;
    const text = refinementInput.trim();
    setActiveRefinements((prev) => [...prev, text]);
    onFetchSummary(text);
    setRefinementInput("");
  };

  return (
    <div className="flex-1 flex flex-col overflow-hidden paper-texture">
      
      {/* Dynamic Header */}
      <header className="sticky top-0 w-full z-30 backdrop-blur-md bg-[#FDFCFB]/80 flex justify-between items-center px-5 py-3 border-b border-slate-100 shrink-0">
        <div className="flex items-center gap-3">
          <div 
            onClick={() => setShowSettings(!showSettings)}
            className="w-10 h-10 rounded-full bg-slate-100 overflow-hidden border-2 border-rose-100 cursor-pointer hover:border-rose-400 active:scale-95 transition-all"
          >
            <img 
              className="w-full h-full object-cover" 
              src={user.avatar} 
              alt={user.name}
              referrerPolicy="no-referrer"
            />
          </div>
          <div>
            <h1 className="font-sans text-2xl font-black text-slate-900 leading-tight">
              {activeTab === "journal" && <>Today<span className="text-rose-500">.</span></>}
              {activeTab === "stats" && <>Stats<span className="text-indigo-500">.</span></>}
              {activeTab === "summary" && <>Confidant<span className="text-purple-500">.</span></>}
            </h1>
            <p className="text-[9px] uppercase tracking-wider font-bold text-slate-400 select-none">Ethereal Journal</p>
          </div>
        </div>

        <div className="flex items-center gap-1.5">
          {/* Quick AI Trigger if in other tabs */}
          {activeTab !== "summary" && (
            <button 
              id="header-ai-quick-btn"
              onClick={handleTriggerAISummary}
              className="px-3.5 py-1.5 bg-gradient-to-r from-indigo-500 to-rose-500 text-white rounded-full transition-all hover:scale-[1.02] active:scale-95 cursor-pointer flex items-center justify-center gap-1 shadow-md shadow-indigo-100"
              title="Consult AI summary"
            >
              <span className="material-symbols-outlined !text-[16px]">auto_awesome</span>
              <span className="text-[10px] font-bold uppercase tracking-wider hidden sm:inline">AI summary</span>
            </button>
          )}

          {/* Settings button */}
          <button 
            id="settings-cog-button"
            onClick={() => setShowSettings(!showSettings)}
            className="p-2 hover:bg-slate-100 rounded-full text-slate-700 transition-colors active:scale-95 cursor-pointer flex items-center justify-center"
          >
            <span className="material-symbols-outlined !text-[20px]">settings</span>
          </button>
        </div>
      </header>

      {/* Main Tab Content Body (Scrollable) */}
      <div className="flex-1 overflow-y-auto px-5 pb-28 pt-4">
        
        {/* TAB 1: JOURNAL VIEW */}
        {activeTab === "journal" && (
          <div className="space-y-5 animate-fade-in">
            {/* Weekly Calendar Carousel */}
            <div className="bg-white rounded-2xl p-4 card-elevation-1 border border-slate-100 select-none">
              <div className="flex justify-between items-center mb-3">
                <span className="text-xs font-bold text-slate-800 uppercase tracking-wider">June 2026</span>
                <span className="text-xs font-medium text-slate-400">Week 26</span>
              </div>
              <div className="flex justify-between">
                {[
                  { day: "Mon", date: "22" },
                  { day: "Tue", date: "23" },
                  { day: "Wed", date: "24" },
                  { day: "Thu", date: "25", active: true },
                  { day: "Fri", date: "26" },
                  { day: "Sat", date: "27" },
                  { day: "Sun", date: "28" },
                ].map((item, index) => (
                  <div 
                    key={index}
                    className={`flex flex-col items-center p-2.5 rounded-xl transition-all w-[42px] ${
                      item.active 
                        ? "bg-gradient-to-br from-indigo-500 via-purple-500 to-rose-500 text-white shadow-lg shadow-indigo-100" 
                        : "text-slate-700 hover:bg-slate-50 cursor-pointer"
                    }`}
                  >
                    <span className={`text-[10px] uppercase font-black tracking-wider ${item.active ? "text-white" : "text-slate-400"}`}>
                      {item.day}
                    </span>
                    <span className="text-sm font-black mt-1">
                      {item.date}
                    </span>
                  </div>
                ))}
              </div>
            </div>

            {/* Dynamic New Entry Panel */}
            <div className="bg-white rounded-[1.8rem] p-5 card-elevation-1 border border-slate-100/80 space-y-4">
              <div className="flex justify-between items-center border-b border-slate-100 pb-2.5">
                <h3 className="font-sans text-base font-black text-slate-800">Record Reflection<span className="text-rose-500">.</span></h3>
                <div className="flex items-center gap-1.5">
                  <button 
                    id="toggle-voice-btn"
                    onClick={() => {
                      setIsVoiceActive(!isVoiceActive);
                      setNewText("");
                    }}
                    className={`flex items-center gap-1 px-3 py-1 rounded-full text-xs font-bold transition-all cursor-pointer ${
                      isVoiceActive 
                        ? "bg-gradient-to-r from-indigo-500 to-rose-500 text-white shadow-md shadow-indigo-100" 
                        : "bg-slate-100 text-slate-500 hover:bg-slate-200"
                    }`}
                  >
                    <span className="material-symbols-outlined !text-[14px]">mic</span>
                    Voice Entry
                  </button>
                </div>
              </div>

              {!isVoiceActive ? (
                /* Traditional Text Entry */
                <div className="space-y-4">
                  <textarea
                    id="entry-textarea"
                    rows={3}
                    placeholder="What is occupying your thoughts today?"
                    value={newText}
                    onChange={(e) => setNewText(e.target.value)}
                    className="w-full text-sm text-slate-800 placeholder-slate-400 bg-slate-50/70 p-3 rounded-xl border border-slate-100/60 focus:outline-none focus:border-rose-300 focus:bg-white transition-all font-sans"
                  />
                  
                  <div className="space-y-2">
                    <span className="text-[10px] uppercase tracking-wider font-bold text-slate-400">Current Mood</span>
                    <div className="flex flex-wrap gap-1.5">
                      {moods.map((m) => (
                        <button
                          key={m.name}
                          onClick={() => setSelectedMood(m.name)}
                          className={`flex items-center gap-1 px-3 py-1.5 border rounded-full text-xs font-bold transition-all cursor-pointer ${
                            selectedMood === m.name
                              ? m.bg + " border-transparent ring-2 ring-offset-1 ring-slate-800"
                              : "border-slate-200 text-slate-500 bg-white hover:bg-slate-50"
                          }`}
                        >
                          <span className="material-symbols-outlined !text-[14px]">{m.icon}</span>
                          {m.name}
                        </button>
                      ))}
                    </div>
                  </div>

                  <div className="flex justify-end pt-1">
                    <button
                      id="save-entry-button"
                      onClick={handleSaveEntry}
                      disabled={!newText.trim()}
                      className="px-5 py-2.5 bg-gradient-to-r from-indigo-500 to-rose-500 text-white rounded-full text-xs font-bold cursor-pointer disabled:opacity-40 disabled:cursor-not-allowed shadow-lg shadow-rose-100 hover:scale-[1.01] active:scale-95 transition-all flex items-center gap-1.5"
                    >
                      <span className="material-symbols-outlined !text-[16px]">edit_note</span>
                      Save Entry
                    </button>
                  </div>
                </div>
              ) : (
                /* Dynamic Voice Dictation Simulation */
                <div className="py-4 flex flex-col items-center justify-center space-y-4">
                  {isRecording ? (
                    <>
                      {/* Dynamic waveform visualization */}
                      <div className="flex items-center gap-1 h-12">
                        {[3, 6, 8, 4, 9, 2, 6, 8, 5, 9, 7, 3, 5, 8, 2, 6, 4].map((h, i) => (
                          <div 
                            key={i}
                            className="w-1 bg-rose-400 rounded-full animate-pulse"
                            style={{ 
                              height: `${h * 4}px`,
                              animationDelay: `${i * 0.1}s`,
                              animationDuration: "0.6s"
                            }}
                          />
                        ))}
                      </div>
                      <span className="font-mono text-sm font-bold text-slate-800">{formattedVoiceTimer()}</span>
                      <button
                        id="stop-recording-button"
                        onClick={stopVoiceRecording}
                        className="w-14 h-14 rounded-full bg-red-500 hover:bg-red-600 flex items-center justify-center text-white shadow-lg cursor-pointer transition-transform active:scale-95 animate-pulse"
                      >
                        <span className="material-symbols-outlined !text-[28px]">stop</span>
                      </button>
                      <p className="text-xs text-slate-400 font-bold uppercase">Listening to vocal reflections...</p>
                    </>
                  ) : (
                    <>
                      <button
                        id="start-recording-button"
                        onClick={startVoiceRecording}
                        className="w-14 h-14 rounded-full bg-gradient-to-br from-indigo-500 to-rose-500 flex items-center justify-center text-white shadow-lg cursor-pointer transition-transform active:scale-95 hover:scale-105"
                      >
                        <span className="material-symbols-outlined !text-[28px]">mic</span>
                      </button>
                      <div className="text-center space-y-1">
                        <p className="text-sm font-black text-slate-800">Tap to dictate your diary</p>
                        <p className="text-xs text-slate-400 font-bold uppercase tracking-wider">Using Sophisticated Voice Engine</p>
                      </div>
                      
                      {newText && (
                        <div className="w-full bg-slate-50 p-3 rounded-xl border border-slate-100 text-xs italic text-slate-700 mt-2 font-sans leading-relaxed">
                          "{newText}"
                        </div>
                      )}

                      {newText && (
                        <button
                          onClick={handleSaveEntry}
                          className="px-5 py-2.5 bg-gradient-to-r from-indigo-500 to-rose-500 text-white rounded-full text-xs font-bold cursor-pointer transition-transform active:scale-95 shadow-md shadow-indigo-100"
                        >
                          Use Transcript
                        </button>
                      )}
                    </>
                  )}
                </div>
              )}
            </div>

            {/* Written Reflections Feed */}
            <div className="space-y-4">
              <h3 className="font-sans text-lg font-black text-slate-800 select-none flex items-center justify-between">
                <span>Today's Reflections<span className="text-rose-500">.</span></span>
                <span className="text-[10px] font-black text-rose-500 bg-rose-50 border border-rose-100 px-3 py-1 rounded-full uppercase tracking-wider select-none">
                  {entries.length} {entries.length === 1 ? "entry" : "entries"}
                </span>
              </h3>

              <div className="space-y-3.5">
                {entries.length === 0 ? (
                  <div className="p-6 text-center border-2 border-dashed border-slate-200 rounded-2xl bg-white/50 text-slate-400">
                    <span className="material-symbols-outlined !text-[36px] text-slate-300">history_edu</span>
                    <p className="font-sans text-base font-black text-slate-700 mt-1">Empty canvas awaits</p>
                    <p className="text-xs">No logs recorded for today. Express your thoughts above!</p>
                  </div>
                ) : (
                  entries.map((entry) => {
                    const moodObj = moods.find((m) => m.name === entry.mood) || moods[4];
                    return (
                      <div 
                        key={entry.id}
                        className="p-4 bg-white rounded-2xl border border-slate-100 shadow-sm relative group animate-fade-in hover:shadow-md transition-shadow"
                      >
                        <div className="flex justify-between items-start mb-2.5">
                          <div className="flex items-center gap-2">
                            <span className="font-mono text-xs text-slate-400 font-semibold bg-slate-100 px-2 py-0.5 rounded-md">
                              {entry.time}
                            </span>
                            <span className={`flex items-center gap-0.5 px-2.5 py-0.5 border rounded-full text-[10px] font-bold ${moodObj.bg}`}>
                              <span className="material-symbols-outlined !text-[10px]">{moodObj.icon}</span>
                              {entry.mood}
                            </span>
                          </div>
                          
                          {/* Delete button */}
                          <button 
                            onClick={() => onDeleteEntry(entry.id)}
                            className="text-slate-300 hover:text-red-500 opacity-60 hover:opacity-100 transition-all p-1 hover:bg-slate-50 rounded-full cursor-pointer"
                            title="Delete entry"
                          >
                            <span className="material-symbols-outlined !text-[18px]">delete</span>
                          </button>
                        </div>

                        <p className="text-slate-800 font-sans text-[14px] leading-relaxed whitespace-pre-line">
                          {entry.text}
                        </p>
                      </div>
                    );
                  })
                )}
              </div>
            </div>

            {/* Quick Trigger Button at the end of the list */}
            {entries.length > 0 && (
              <div className="pt-4 flex justify-center">
                <button
                  onClick={handleTriggerAISummary}
                  className="w-full flex items-center justify-center gap-2 bg-gradient-to-r from-indigo-500 via-purple-500 to-rose-500 text-white py-3.5 px-6 rounded-2xl shadow-xl shadow-rose-50 active:scale-95 transition-all cursor-pointer"
                >
                  <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>
                    auto_awesome
                  </span>
                  <span className="font-sans font-black text-xs tracking-widest uppercase">Generate Confidant Summary</span>
                </button>
              </div>
            )}
          </div>
        )}

        {/* TAB 2: METRICS / STATS VIEW */}
        {activeTab === "stats" && (
          <div className="space-y-5 animate-fade-in">
            {/* Elegant Device 2 layout wrapper: Sleek container with ring chart */}
            <div className="bg-slate-950 rounded-[2.5rem] p-6 border-[6px] border-[#1E293B] shadow-2xl relative overflow-hidden flex flex-col items-center">
              <div className="w-full flex justify-between items-center mb-6">
                <div>
                  <span className="text-[10px] text-slate-500 uppercase font-bold tracking-widest">Ethereal Vector</span>
                  <h3 className="font-sans text-xl font-black text-white leading-none">Ethereal State</h3>
                </div>
                <div className="w-8 h-8 rounded-full bg-slate-900 border border-slate-800 flex items-center justify-center">
                  <span className="material-symbols-outlined text-emerald-400 !text-[16px]">bolt</span>
                </div>
              </div>

              {/* Ring Chart Area */}
              <div className="relative w-44 h-44 flex items-center justify-center my-2">
                {/* SVG Radial progress bar */}
                <svg className="absolute w-full h-full -rotate-90">
                  <circle
                    cx="88"
                    cy="88"
                    r="72"
                    className="stroke-slate-900 fill-none"
                    strokeWidth="14"
                  />
                  <circle
                    cx="88"
                    cy="88"
                    r="72"
                    className="stroke-emerald-400 fill-none transition-all duration-1000 ease-out"
                    strokeWidth="14"
                    strokeDasharray={2 * Math.PI * 72}
                    strokeDashoffset={2 * Math.PI * 72 * (1 - energyPercent / 100)}
                    strokeLinecap="round"
                  />
                </svg>
                
                {/* Center text interaction */}
                <div 
                  onClick={() => setEnergyPercent((prev) => (prev >= 100 ? 50 : prev + 8))}
                  className="flex flex-col items-center justify-center cursor-pointer select-none group z-10 w-28 h-28 rounded-full hover:bg-slate-900/50 transition-colors"
                >
                  <span className="text-3xl font-black text-white group-hover:scale-105 transition-transform">
                    {energyPercent}<span className="text-emerald-400 text-lg">%</span>
                  </span>
                  <span className="text-[9px] text-slate-500 uppercase tracking-widest font-bold">Energy</span>
                  <span className="text-[8px] text-emerald-400 font-bold uppercase tracking-widest scale-0 group-hover:scale-100 transition-all">Tap to Boost</span>
                </div>
              </div>

              <p className="text-[11px] text-slate-400 text-center px-4 mt-2">
                Your energy level fluctuates in real-time based on the emotional frequencies registered in your diary logs.
              </p>

              {/* Data Pills Styled like Device 2 */}
              <div className="grid grid-cols-2 gap-4 w-full mt-6">
                <div className="bg-slate-900/80 p-4 rounded-3xl border border-slate-800 flex flex-col justify-between">
                  <div>
                    <div className="flex justify-between items-center mb-1">
                      <p className="text-[9px] text-slate-500 uppercase font-black tracking-wider">Steps</p>
                      <span className="material-symbols-outlined text-rose-400 !text-[12px]">directions_walk</span>
                    </div>
                    <p className="text-white font-black text-lg">{simulatedSteps.toLocaleString()}</p>
                  </div>
                  <div className="flex gap-1.5 mt-2.5">
                    <button 
                      onClick={() => setSimulatedSteps(prev => prev + 500)}
                      className="flex-1 py-1 bg-slate-800 hover:bg-slate-700 active:scale-95 transition-all text-[10px] text-slate-200 font-bold rounded-lg cursor-pointer"
                    >
                      +500
                    </button>
                    <button 
                      onClick={() => setSimulatedSteps(prev => Math.max(0, prev - 500))}
                      className="flex-1 py-1 bg-slate-800 hover:bg-slate-700 active:scale-95 transition-all text-[10px] text-slate-200 font-bold rounded-lg cursor-pointer"
                    >
                      -500
                    </button>
                  </div>
                </div>

                <div className="bg-slate-900/80 p-4 rounded-3xl border border-slate-800 flex flex-col justify-between">
                  <div>
                    <div className="flex justify-between items-center mb-1">
                      <p className="text-[9px] text-slate-500 uppercase font-black tracking-wider">Sleep</p>
                      <span className="material-symbols-outlined text-indigo-400 !text-[12px]">bedtime</span>
                    </div>
                    <p className="text-white font-black text-lg">{simulatedSleep.hours}h {simulatedSleep.minutes}m</p>
                  </div>
                  <div className="flex gap-1.5 mt-2.5">
                    <button 
                      onClick={() => setSimulatedSleep(prev => {
                        let mins = prev.minutes + 15;
                        let hrs = prev.hours;
                        if (mins >= 60) { mins = 0; hrs++; }
                        return { hours: hrs, minutes: mins };
                      })}
                      className="flex-1 py-1 bg-slate-800 hover:bg-slate-700 active:scale-95 transition-all text-[10px] text-slate-200 font-bold rounded-lg cursor-pointer"
                    >
                      +15m
                    </button>
                    <button 
                      onClick={() => setSimulatedSleep(prev => {
                        let mins = prev.minutes - 15;
                        let hrs = prev.hours;
                        if (mins < 0) { mins = 45; hrs = Math.max(0, hrs - 1); }
                        return { hours: hrs, minutes: mins };
                      })}
                      className="flex-1 py-1 bg-slate-800 hover:bg-slate-700 active:scale-95 transition-all text-[10px] text-slate-200 font-bold rounded-lg cursor-pointer"
                    >
                      -15m
                    </button>
                  </div>
                </div>
              </div>
            </div>

            {/* Sub Metrics list */}
            <div className="bg-white rounded-3xl p-5 card-elevation-1 border border-slate-100 space-y-4">
              <h4 className="font-sans text-base font-black text-slate-800">Journal Analytics</h4>
              
              {/* Word Count Metric */}
              <div className="flex items-center justify-between p-3.5 bg-slate-50 rounded-2xl border border-slate-100">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-orange-100 text-orange-500 flex items-center justify-center font-bold">
                    <span className="material-symbols-outlined">notes</span>
                  </div>
                  <div>
                    <p className="text-xs text-slate-400 uppercase tracking-wider font-bold">Total Words Logged</p>
                    <p className="text-sm font-black text-slate-800">{getTotalLoggedWords()} words</p>
                  </div>
                </div>
                <span className="text-xs text-slate-400 font-bold">June 25</span>
              </div>

              {/* Mood breakdown */}
              <div className="space-y-2.5">
                <span className="text-[10px] uppercase tracking-widest text-slate-400 font-black">Logged Frequency by Mood</span>
                <div className="space-y-2">
                  {moods.map((m) => {
                    const count = entries.filter(e => e.mood === m.name).length;
                    const pct = entries.length ? Math.round((count / entries.length) * 100) : 0;
                    return (
                      <div key={m.name} className="flex items-center justify-between text-xs font-bold text-slate-700">
                        <div className="flex items-center gap-1.5 w-24 shrink-0">
                          <span className="material-symbols-outlined !text-[14px]">{m.icon}</span>
                          <span>{m.name}</span>
                        </div>
                        <div className="flex-1 mx-3 bg-slate-100 h-2 rounded-full overflow-hidden">
                          <div 
                            className="bg-indigo-500 h-full rounded-full transition-all duration-500"
                            style={{ width: `${pct || 4}%` }}
                          />
                        </div>
                        <span className="text-[11px] text-slate-400 w-10 text-right">{count} ({pct}%)</span>
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 3: AI CONFIDANT SUMMARY VIEW */}
        {activeTab === "summary" && (
          <div className="space-y-5 animate-fade-in pb-16">
            {summaryLoading ? (
              <div className="py-16 flex flex-col items-center justify-center space-y-6 text-center">
                <div className="relative flex items-center justify-center">
                  <div className="w-20 h-20 rounded-full border-4 border-indigo-100 border-t-rose-500 animate-spin" />
                  <span className="material-symbols-outlined text-rose-500 absolute animate-pulse !text-[32px]">
                    auto_awesome
                  </span>
                </div>
                <div className="space-y-2">
                  <p className="font-sans text-lg font-black text-slate-800">"Consulting Ethereal Mind..."</p>
                  <p className="text-xs text-slate-400 font-bold uppercase tracking-wider animate-pulse">Reading reflections, mapping emotional trajectories</p>
                </div>
              </div>
            ) : !summary ? (
              <div className="py-12 flex flex-col items-center justify-center text-center space-y-4">
                <div className="w-16 h-16 rounded-full bg-indigo-50 text-indigo-500 flex items-center justify-center animate-bounce">
                  <span className="material-symbols-outlined !text-[32px]">psychology</span>
                </div>
                <div className="space-y-1 px-4">
                  <h4 className="font-sans text-lg font-black text-slate-800">No summary generated yet</h4>
                  <p className="text-xs text-slate-500 leading-relaxed max-w-xs">
                    Allow Gemini to read your current emotional patterns and produce milestones and actionable vectors.
                  </p>
                </div>
                <button
                  onClick={() => onFetchSummary()}
                  className="px-6 py-3 bg-gradient-to-r from-indigo-500 via-purple-500 to-rose-500 text-white rounded-full text-xs font-black uppercase tracking-widest shadow-lg shadow-rose-100 hover:scale-105 active:scale-95 transition-all cursor-pointer"
                >
                  Generate Daily Insight
                </button>
              </div>
            ) : (
              <div className="space-y-5 animate-fade-in">
                
                {/* Atmosphere visual banner */}
                <div className="relative w-full h-32 rounded-3xl overflow-hidden bg-gradient-to-br from-indigo-500 via-purple-500 to-rose-500 shadow-lg shadow-indigo-100 flex items-center justify-center p-5">
                  <div className="absolute inset-0 bg-gradient-to-t from-indigo-900/10 via-transparent to-transparent" />
                  
                  <div className="flex items-center gap-4 relative z-10 w-full">
                    <div className="w-12 h-12 rounded-2xl bg-white/20 backdrop-blur-md flex items-center justify-center">
                      <span className="material-symbols-outlined text-white" style={{ fontVariationSettings: "'FILL' 1" }}>
                        psychology
                      </span>
                    </div>
                    <div>
                      <p className="text-[9px] uppercase tracking-widest text-white/80 font-black">Confidant Reflection</p>
                      <p className="font-sans text-sm font-black text-white leading-tight">Daily emotional vectors stabilized</p>
                      <p className="text-[10px] text-white/70 italic mt-0.5">Updated: June 25, 2026</p>
                    </div>
                  </div>

                  <div className="absolute top-4 right-4">
                    <span className="px-2.5 py-0.5 bg-white/20 backdrop-blur-md text-white rounded-full text-[9px] font-black uppercase tracking-wider">
                      DEEP INSIGHT
                    </span>
                  </div>
                </div>

                {/* 1. Milestones */}
                <div className="border-l-4 border-indigo-500 pl-4 py-3 space-y-2 bg-white rounded-r-2xl border border-slate-100 shadow-sm">
                  <div className="flex items-center gap-2 text-indigo-500 font-bold">
                    <span className="material-symbols-outlined">auto_awesome</span>
                    <h3 className="font-sans text-sm font-black uppercase tracking-wider text-slate-800">Milestones</h3>
                  </div>
                  <ul className="list-disc list-inside text-slate-600 font-sans text-xs leading-relaxed space-y-1.5 pl-1">
                    {summary.milestones.map((milestone, idx) => (
                      <li key={idx} className="marker:text-indigo-500">{milestone}</li>
                    ))}
                  </ul>
                </div>

                {/* 2. Actionable Vectors */}
                <div className="border-l-4 border-rose-500 pl-4 py-3 space-y-2 bg-white rounded-r-2xl border border-slate-100 shadow-sm">
                  <div className="flex items-center gap-2 text-rose-500 font-bold">
                    <span className="material-symbols-outlined">trending_up</span>
                    <h3 className="font-sans text-sm font-black uppercase tracking-wider text-slate-800">Actionable Vectors</h3>
                  </div>
                  <ul className="list-disc list-inside text-slate-600 font-sans text-xs leading-relaxed space-y-1.5 pl-1">
                    {summary.actionableVectors.map((vector, idx) => (
                      <li key={idx} className="marker:text-rose-500">{vector}</li>
                    ))}
                  </ul>
                </div>

                {/* 3. Emotional Tone */}
                <div className="border-l-4 border-amber-400 pl-4 py-3 space-y-2 bg-white rounded-r-2xl border border-slate-100 shadow-sm">
                  <div className="flex items-center gap-2 text-amber-500 font-bold">
                    <span className="material-symbols-outlined">sentiment_satisfied</span>
                    <h3 className="font-sans text-sm font-black uppercase tracking-wider text-slate-800">Emotional Tone</h3>
                  </div>
                  <ul className="list-disc list-inside text-slate-600 font-sans text-xs leading-relaxed space-y-1.5 pl-1">
                    {summary.emotionalTone.map((tone, idx) => (
                      <li key={idx} className="marker:text-amber-500">{tone}</li>
                    ))}
                  </ul>
                </div>

                {/* Active Refinements list in conversation format */}
                {activeRefinements.length > 0 && (
                  <div className="space-y-3 pt-2">
                    <span className="text-[10px] font-black uppercase tracking-widest text-slate-400">Refinement History</span>
                    <div className="space-y-2.5">
                      {activeRefinements.map((ref, idx) => (
                        <div key={idx} className="flex gap-2 items-start justify-end">
                          <div className="bg-rose-50 border border-rose-100 text-rose-700 text-xs py-2 px-3 rounded-2xl rounded-tr-none max-w-[80%] leading-relaxed font-sans">
                            {ref}
                          </div>
                          <span className="material-symbols-outlined text-rose-400 !text-[16px] mt-1">account_circle</span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Copy/Share Floating Chips */}
                <div className="flex gap-2 justify-center pt-2 select-none">
                  <button 
                    id="copy-summary-btn"
                    onClick={handleCopySummary}
                    className="flex-1 flex items-center justify-center gap-1.5 py-2.5 border border-indigo-200 rounded-xl text-xs font-black text-indigo-500 bg-indigo-50/40 hover:bg-indigo-50 transition-colors active:scale-95 cursor-pointer"
                  >
                    <span className="material-symbols-outlined !text-[16px]">
                      {copied ? "check" : "content_copy"}
                    </span>
                    {copied ? "Copied" : "Copy Insight"}
                  </button>
                  <button 
                    id="share-summary-btn"
                    onClick={() => setShowShareModal(true)}
                    className="flex-1 flex items-center justify-center gap-1.5 py-2.5 border border-slate-200 rounded-xl text-xs font-black text-slate-700 bg-slate-50/50 hover:bg-slate-100 transition-colors active:scale-95 cursor-pointer"
                  >
                    <span className="material-symbols-outlined !text-[16px]">ios_share</span>
                    Share Log
                  </button>
                </div>

                {/* Inline Chat Refinement Form at bottom of summary tab */}
                <form onSubmit={handleRefineSubmit} className="mt-4 flex gap-2">
                  <input
                    type="text"
                    placeholder="Ask Gemini to refine (e.g. 'make it warm')"
                    value={refinementInput}
                    onChange={(e) => setRefinementInput(e.target.value)}
                    className="flex-1 text-xs text-slate-800 placeholder-slate-400 bg-white border border-slate-200 p-3 rounded-xl focus:outline-none focus:border-purple-400 transition-all font-sans"
                  />
                  <button
                    type="submit"
                    disabled={!refinementInput.trim()}
                    className="p-3 bg-purple-500 hover:bg-purple-600 disabled:opacity-40 text-white rounded-xl flex items-center justify-center transition-all active:scale-95 cursor-pointer shadow-md shadow-indigo-100"
                  >
                    <span className="material-symbols-outlined !text-[16px]">send</span>
                  </button>
                </form>
              </div>
            )}
          </div>
        )}
      </div>

      {/* FIXED BOTTOM NAVIGATION BAR (Inspired by Stitch/Mockup) */}
      <div className="absolute bottom-0 inset-x-0 h-22 bg-white/95 backdrop-blur-md border-t border-slate-100 flex items-center justify-around px-6 pb-2 z-40 shrink-0">
        
        {/* Tab 1: Journal */}
        <div 
          onClick={() => setActiveTab("journal")}
          className="flex flex-col items-center justify-center cursor-pointer select-none group w-14"
        >
          <div className={`w-11 h-11 rounded-2xl flex items-center justify-center transition-all duration-300 ${
            activeTab === "journal" 
              ? "bg-rose-500 text-white shadow-lg shadow-rose-200 scale-105" 
              : "text-slate-400 hover:bg-slate-50"
          }`}>
            <span className="material-symbols-outlined !text-[22px]">history_edu</span>
          </div>
          <span className={`text-[9px] font-black uppercase tracking-wider mt-1 transition-colors ${
            activeTab === "journal" ? "text-rose-500" : "text-slate-400"
          }`}>
            Journal
          </span>
        </div>

        {/* Tab 2: Stats */}
        <div 
          onClick={() => setActiveTab("stats")}
          className="flex flex-col items-center justify-center cursor-pointer select-none group w-14"
        >
          <div className={`w-11 h-11 rounded-2xl flex items-center justify-center transition-all duration-300 ${
            activeTab === "stats" 
              ? "bg-[#1E293B] text-white shadow-lg shadow-slate-300 scale-105" 
              : "text-slate-400 hover:bg-slate-50"
          }`}>
            <span className="material-symbols-outlined !text-[22px]">query_stats</span>
          </div>
          <span className={`text-[9px] font-black uppercase tracking-wider mt-1 transition-colors ${
            activeTab === "stats" ? "text-slate-800" : "text-slate-400"
          }`}>
            Stats
          </span>
        </div>

        {/* Tab 3: Confidant AI */}
        <div 
          onClick={() => {
            setActiveTab("summary");
            if (!summary) onFetchSummary();
          }}
          className="flex flex-col items-center justify-center cursor-pointer select-none group w-14"
        >
          <div className={`w-11 h-11 rounded-2xl flex items-center justify-center transition-all duration-300 ${
            activeTab === "summary" 
              ? "bg-indigo-500 text-white shadow-lg shadow-indigo-200 scale-105" 
              : "text-slate-400 hover:bg-slate-50"
          }`}>
            <span className="material-symbols-outlined !text-[22px]">auto_awesome</span>
          </div>
          <span className={`text-[9px] font-black uppercase tracking-wider mt-1 transition-colors ${
            activeTab === "summary" ? "text-indigo-500" : "text-slate-400"
          }`}>
            Confidant
          </span>
        </div>
      </div>

      {/* Share Modal Dialog */}
      {showShareModal && (
        <div className="absolute inset-0 bg-black/40 backdrop-blur-sm z-50 flex items-center justify-center p-5">
          <div className="bg-white rounded-[2rem] p-6 space-y-4 max-w-xs w-full shadow-2xl animate-scale-in">
            <div className="flex justify-between items-center pb-2 border-b border-slate-100">
              <h4 className="font-sans text-sm font-black text-slate-800 uppercase tracking-widest">Share Diary Insight</h4>
              <button 
                onClick={() => setShowShareModal(false)}
                className="p-1 hover:bg-slate-100 rounded-full cursor-pointer"
              >
                <span className="material-symbols-outlined text-slate-400 !text-[18px]">close</span>
              </button>
            </div>
            
            <p className="text-xs text-slate-500 leading-relaxed text-center py-2">
              Share your daily milestones and emotional vector insights securely.
            </p>

            <div className="grid grid-cols-2 gap-2.5">
              <button 
                onClick={() => {
                  alert("Link copied! Ready to share with your coach/confidant.");
                  setShowShareModal(false);
                }}
                className="flex flex-col items-center gap-1.5 p-3 border border-slate-100 rounded-xl hover:bg-rose-50/40 text-slate-700 hover:text-rose-500 transition-all cursor-pointer"
              >
                <span className="material-symbols-outlined !text-[24px]">link</span>
                <span className="text-[10px] font-bold">Copy Link</span>
              </button>
              <button 
                onClick={() => {
                  alert("Message export assembled successfully.");
                  setShowShareModal(false);
                }}
                className="flex flex-col items-center gap-1.5 p-3 border border-slate-100 rounded-xl hover:bg-indigo-50/40 text-slate-700 hover:text-indigo-500 transition-all cursor-pointer"
              >
                <span className="material-symbols-outlined !text-[24px]">sms</span>
                <span className="text-[10px] font-bold">SMS Invite</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Settings Panel Backdrop/Modal */}
      {showSettings && (
        <div className="absolute inset-0 bg-black/40 backdrop-blur-sm z-50 flex flex-col justify-end">
          <div className="bg-white rounded-t-[32px] p-6 space-y-5 shadow-2xl animate-slide-up">
            <div className="flex justify-between items-center border-b border-slate-100 pb-3">
              <h4 className="font-sans text-base font-black text-slate-800">Settings & Profile</h4>
              <button 
                onClick={() => setShowSettings(false)}
                className="p-1 hover:bg-slate-100 rounded-full cursor-pointer"
              >
                <span className="material-symbols-outlined text-slate-400">close</span>
              </button>
            </div>

            <div className="flex items-center gap-4 p-3 bg-slate-50 rounded-2xl border border-slate-100/60">
              <img 
                className="w-12 h-12 rounded-full object-cover border-2 border-rose-100" 
                src={user.avatar} 
                alt={user.name}
                referrerPolicy="no-referrer"
              />
              <div>
                <p className="font-black text-sm text-slate-800">{user.name}</p>
                <p className="text-xs text-slate-400 font-bold">{user.email}</p>
              </div>
            </div>

            <div className="space-y-2">
              <button 
                id="sign-out-btn"
                onClick={() => {
                  setShowSettings(false);
                  onSignOut();
                }}
                className="w-full flex items-center justify-between p-3.5 bg-slate-50 rounded-xl hover:bg-red-50 text-red-600 hover:text-red-700 transition-colors cursor-pointer text-left font-bold text-sm"
              >
                <span>Sign Out Account</span>
                <span className="material-symbols-outlined !text-[18px]">logout</span>
              </button>

              <button 
                onClick={() => {
                  localStorage.removeItem("ethereal_diary_entries_v1");
                  localStorage.removeItem("ethereal_user_session_v1");
                  window.location.reload();
                }}
                className="w-full flex items-center justify-between p-3.5 bg-slate-50 rounded-xl hover:bg-slate-100 text-slate-700 transition-colors cursor-pointer text-left font-bold text-sm"
              >
                <span>Reset Application Storage</span>
                <span className="material-symbols-outlined !text-[18px]">restart_alt</span>
              </button>
            </div>

            <div className="text-center pt-2 text-[9px] text-slate-400 uppercase tracking-widest font-black select-none">
              Secured with Google Cloud Ingress
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
